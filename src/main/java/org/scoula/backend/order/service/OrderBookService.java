package org.scoula.backend.order.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

import org.scoula.backend.order.controller.response.OrderBookResponse;
import org.scoula.backend.order.controller.response.OrderSnapshotResponse;
import org.scoula.backend.order.controller.response.OrderSummaryResponse;
import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.scoula.backend.order.dto.PriceLevelDto;

import lombok.extern.slf4j.Slf4j;

// 개별 종목의 주문장
@Slf4j
public class OrderBookService {
	// 종목 번호
	private final String companyCode;
	// 매도 주문: 낮은 가격 우선
	private final TreeMap<BigDecimal, Queue<Order>> askOrders = new TreeMap<>();
	// 매수 주문: 높은 가격 우선
	private final TreeMap<BigDecimal, Queue<Order>> bidOrders = new TreeMap<>(Collections.reverseOrder());

	public OrderBookService(final String companyCode) {
		this.companyCode = companyCode;
	}

	public void received(final Order order) {
		// final Order order = new OrderDto(request).to();
		if (order.getType() == Type.BUY) {
			matchBuyOrder(order);
		} else {
			matchSellOrder(order);
		}
	}

	private void matchSellOrder(final Order sellOrder) {
		while (sellOrder.getRemainingQuantity() > 0) {
			log.info("매도 메서드 진입");
			// 매도가보다 높거나 같은 매수 주문 찾기
			Map.Entry<BigDecimal, Queue<Order>> bestBid = bidOrders.firstEntry();

			if (bestBid == null || bestBid.getKey().compareTo(sellOrder.getPrice()) < 0) {
				// 매칭되는 매수 주문이 없으면 주문장에 추가
				log.info("매도 초기값 할당 조건문 진입");
				addToOrderBook(askOrders, sellOrder);
				break;
			}

			// 주문 매칭 처리
			matchOrders(bestBid.getValue(), sellOrder);

			// 매수 큐가 비었으면 제거
			if (bestBid.getValue().isEmpty()) {
				bidOrders.remove(bestBid.getKey());
			}
		}
	}

	private void matchBuyOrder(final Order buyOrder) {
		while (buyOrder.getRemainingQuantity() > 0) {
			log.info("매수 메서드 진입");
			// 매수가보다 낮거나 같은 매도 주문 찾기
			Map.Entry<BigDecimal, Queue<Order>> bestAsk = askOrders.firstEntry();

			if (bestAsk == null || bestAsk.getKey().compareTo(buyOrder.getPrice()) > 0) {
				log.info("매수 초기값 할당 조건문 진입");
				// 매칭되는 매도 주문이 없으면 주문장에 추가
				addToOrderBook(bidOrders, buyOrder);
				break;
			}

			// 주문 매칭 처리
			matchOrders(bestAsk.getValue(), buyOrder);

			// 매수 큐가 비었으면 제거
			if (bestAsk.getValue().isEmpty()) {
				askOrders.remove(bestAsk.getKey());
			}
		}
	}

	private void matchOrders(final Queue<Order> existingOrders, final Order incomingOrder) {
		while (!existingOrders.isEmpty() && incomingOrder.getRemainingQuantity() > 0) {
			final Order existingOrder = existingOrders.peek();

			final Integer matchedQuantity = Math.min(
					incomingOrder.getRemainingQuantity(),
					existingOrder.getRemainingQuantity()
			);

			// 거래 채결 -> OrderHistory
			// excuteTransaction(incomingOrder, existingOrder, matchedQuantity);

			// 수량 업데이트
			// incomingOrder.setRemainingQuantity(
			// 		incomingOrder.getRemainingQuantity() - matchedQuantity
			// );
			// existingOrder.setRemainingQuantity(
			// 		existingOrder.getRemainingQuantity() - matchedQuantity
			// );
			//수량
			incomingOrder.updateQuantity(matchedQuantity);
			existingOrder.updateQuantity(matchedQuantity);

			// 완전 체결된 주문 제거
			if (existingOrder.getRemainingQuantity() == 0) {
				existingOrders.poll();
			}
		}
	}

	// private void excuteTransaction(final Order incomingOrder, final Order existingOrder, final Integer matchedQuantity) {
	// 	BigDecimal executionPrice = existingOrder.getPrice();
	// 	Transaction transaction = new Transaction(
	// 			incomingOrder.getId(),
	// 			existingOrder.getId(),
	// 			executionPrice,
	// 			matchedQuantity,
	// 			LocalDateTime.now();
	// 	)
	// }

	private void addToOrderBook(final TreeMap<BigDecimal, Queue<Order>> orderBook, final Order order) {
		orderBook.computeIfAbsent(
				order.getPrice(),
				k -> new PriorityQueue<>(
						Comparator.comparing(Order::getTimestamp))
		).offer(order);

		// orderBook.computeIfAbsent(
		// 		order.getPrice(),
		// 		k -> new PriorityQueue<>()
		// ).offer(order);
	}

	// // 현재 호가창 스냅샷 생성
	// public OrderSnapshotResponse getSnapshot() {
	// 	return new OrderSnapshotResponse(askOrders, bidOrders);
	// }

	// 종목별 주문장 스냅샷 생성
	public OrderSnapshotResponse getSnapshot() {
		return new OrderSnapshotResponse(companyCode, askOrders, bidOrders);
	}

	// 호가창 생성
	public OrderBookResponse getBook() {
		return OrderBookResponse.builder()
				.companyCode(companyCode)
				.askLevels(createAskLevels())
				.bidLevels(createBidLevels())
				.build();
	}

	// 매도 호가창 정보
	private List<PriceLevelDto> createAskLevels() {
		return this.askOrders.entrySet().stream()
				.limit(5)
				.sorted(Map.Entry.<BigDecimal, Queue<Order>>comparingByKey().reversed()) // 역순 정렬
				.map(entry -> new PriceLevelDto(
						entry.getKey(), calculateTotalQuantity(entry.getValue()), entry.getValue().size())
				).toList();
	}

	// 매수 호가창 정보
	private List<PriceLevelDto> createBidLevels() {
		return this.bidOrders.entrySet().stream()
				.limit(5)
				.map(entry -> new PriceLevelDto(
						entry.getKey(), calculateTotalQuantity(entry.getValue()), entry.getValue().size())
				).toList();
	}

	private Integer calculateTotalQuantity(Queue<Order> orders) {
		return orders.stream()
				.mapToInt(Order::getRemainingQuantity)
				.sum();
	}

	// 종목별 요약 정보 조회
	public OrderSummaryResponse getSummary() {
		// log.info(askOrders.toString());
		// log.info(bidOrders.toString());
		// OrderSnapshotResponse snapshot = getSnapshot();

		return new OrderSummaryResponse(
				// snapshot.getMidPrice(),
				// snapshot.getSpread(),
				companyCode,
				getOrderVolumeStats(askOrders),
				getOrderVolumeStats(bidOrders)
		);
	}

	// 주문 수량 분석
	public Integer getOrderVolumeStats(final TreeMap<BigDecimal, Queue<Order>> orderMap) {
		int count = 0;
		for (Queue<Order> orders : orderMap.values()) {
			for (Order order : orders) {
				count++;
			}
		}
		return count;
	}

}
