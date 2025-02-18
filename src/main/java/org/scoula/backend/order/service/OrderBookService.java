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
	private final TreeMap<BigDecimal, Queue<Order>> sellOrders = new TreeMap<>();
	// 매수 주문: 높은 가격 우선
	private final TreeMap<BigDecimal, Queue<Order>> buyOrders = new TreeMap<>(Collections.reverseOrder());

	public OrderBookService(final String companyCode) {
		this.companyCode = companyCode;
	}

	// TODO : 채결 완료된 주문 history 저장 필요
	// TODO : 채결 기준 (-15% - +15%) 적용 필요
	public void received(final Order order) {
		if (order.getType() == Type.BUY) {
			matchBuyOrder(order);
		} else {
			matchSellOrder(order);
		}
	}

	private void matchSellOrder(final Order order) {
		while (order.getRemainingQuantity() > 0) {
			log.info("매도 메서드 진입");
			// 매도가보다 높거나 같은 매수 주문 찾기
			Map.Entry<BigDecimal, Queue<Order>> bestBid = buyOrders.firstEntry();

			if (bestBid == null || bestBid.getKey().compareTo(order.getPrice()) < 0) {
				// 매칭되는 매수 주문이 없으면 주문장에 추가
				log.info("매도 초기값 할당 조건문 진입");
				addToOrderBook(sellOrders, order);
				break;
			}

			// 주문 매칭 처리
			matchOrders(bestBid.getValue(), order);

			// 매수 큐가 비었으면 제거
			if (bestBid.getValue().isEmpty()) {
				buyOrders.remove(bestBid.getKey());
			}
		}
	}

	private void matchBuyOrder(final Order buyOrder) {
		while (buyOrder.getRemainingQuantity() > 0) {
			log.info("매수 메서드 진입");
			// 매수가보다 낮거나 같은 매도 주문 찾기
			Map.Entry<BigDecimal, Queue<Order>> bestAsk = sellOrders.firstEntry();

			if (bestAsk == null || bestAsk.getKey().compareTo(buyOrder.getPrice()) > 0) {
				log.info("매수 초기값 할당 조건문 진입");
				// 매칭되는 매도 주문이 없으면 주문장에 추가
				addToOrderBook(buyOrders, buyOrder);
				break;
			}

			// 주문 매칭 처리
			matchOrders(bestAsk.getValue(), buyOrder);

			// 매수 큐가 비었으면 제거
			if (bestAsk.getValue().isEmpty()) {
				sellOrders.remove(bestAsk.getKey());
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

	private void addToOrderBook(final TreeMap<BigDecimal, Queue<Order>> orderBook, final Order order) {
		orderBook.computeIfAbsent(
				order.getPrice(),
				k -> new PriorityQueue<>(
						Comparator.comparing(Order::getTimestamp))
		).offer(order);
	}

	// 종목별 주문장 스냅샷 생성
	public OrderSnapshotResponse getSnapshot() {
		return new OrderSnapshotResponse(companyCode, sellOrders, buyOrders);
	}

	// 호가창 생성
	public OrderBookResponse getBook() {
		return OrderBookResponse.builder()
				.companyCode(companyCode)
				.sellLevels(createAskLevels())
				.buyLevels(createBidLevels())
				.build();
	}

	// 매도 호가창 정보
	private List<PriceLevelDto> createAskLevels() {
		return this.sellOrders.entrySet().stream()
				.limit(5)
				.sorted(Map.Entry.<BigDecimal, Queue<Order>>comparingByKey().reversed()) // 역순 정렬
				.map(entry -> new PriceLevelDto(
						entry.getKey(), calculateTotalQuantity(entry.getValue()), entry.getValue().size())
				).toList();
	}

	// 매수 호가창 정보
	private List<PriceLevelDto> createBidLevels() {
		return this.buyOrders.entrySet().stream()
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
		return new OrderSummaryResponse(
				companyCode,
				getOrderVolumeStats(sellOrders),
				getOrderVolumeStats(buyOrders)
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
