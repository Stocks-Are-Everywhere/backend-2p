package org.scoula.backend.order.service;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.scoula.backend.order.service.multiqueue.CompanyOrderBook;
import org.scoula.backend.order.service.multiqueue.MarketCondition;
import org.scoula.backend.order.service.multiqueue.MarketStateCache;
import org.scoula.backend.order.service.multiqueue.OrderBookManager;
import org.scoula.backend.order.service.multiqueue.OrderQueue;
import org.scoula.backend.order.service.multiqueue.OrderRequest;
import org.scoula.backend.order.service.multiqueue.monitoring.PerformanceTracker;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

	private final PerformanceTracker performanceTracker;
	private final OrderBookManager orderBookManager;
	private final MarketStateCache marketStateCache;

	@Override
	public void ProcessMarketData(String data) {
		try {
			// 1. 시세 데이터 파싱
			MarketCondition condition = MarketCondition.parse(data);

			// 2. 캐시 업데이트
			updateCondition(condition);

			// 3. 체결 프로세스 실행
			processMatching(condition.getCompanyCode());

			log.info("Processed market data for: {}", condition.getCompanyCode());
		} catch (Exception e) {
			log.error("Failed to process market data: {}", data, e);
		}
	}

	//-----------------engine--------------------------------------

	public boolean processMatching(String companyCode) {
		MarketCondition condition = getCondition(companyCode);
		if (condition == null) {
			log.warn("No market condition found for: {}", companyCode);
			return false;
		}

		CompanyOrderBook orderBook = orderBookManager.getOrderBook(companyCode);
		processBuyOrders(orderBook, condition);
		processSellOrders(orderBook, condition);
		return true;
	}

	private void processBuyOrders(CompanyOrderBook orderBook, MarketCondition condition) {
		OrderQueue buyQueue = orderBook.getBuyQueue(condition.getCurrentPrice());
		if (buyQueue == null || buyQueue.isEmpty())
			return;

		while (!buyQueue.isEmpty()) {
			OrderRequest order = buyQueue.peek();
			if (!condition.isMatchableBuy(order))
				break;
			buyQueue.poll();
			log.info("Matched buy order: {}", order);
		}
	}

	private void processSellOrders(CompanyOrderBook orderBook, MarketCondition condition) {
		OrderQueue sellQueue = orderBook.getSellQueue(condition.getCurrentPrice());
		if (sellQueue == null || sellQueue.isEmpty())
			return;

		while (!sellQueue.isEmpty()) {
			OrderRequest order = sellQueue.peek();
			if (!condition.isMatchableSell(order))
				break;
			sellQueue.poll();
			log.info("Matched sell order: {}", order);
		}
	}

	public MarketCondition getCondition(String companyCode) {
		return marketStateCache.getCondition(companyCode);
	}

	public void updateCondition(MarketCondition condition) {
		marketStateCache.updateCondition(condition);
		log.info("Updated market condition for: {}", condition.getCompanyCode());
	}

	//----------------------------------------------------------------------------------------------------

	@Override
	public Boolean ProcessOrder(Order order) {
		String orderId = order.getId().toString();
		try {
			// 주문 생성 시점 추적
			performanceTracker.trackOrderCreation(order);

			// 처리 시작 시점 명확히 추적
			performanceTracker.trackOrderProcessingStart(orderId);

			// 주문 파싱 및 매칭 로직
			OrderRequest orderRequest = parsingToOrderRequest(order);
			boolean matchResult = processNewOrder(orderRequest);

			// 처리 완료 시점 추적
			performanceTracker.trackOrderMatched(orderId, matchResult);

			return matchResult;
		} catch (Exception e) {
			// 실패 시점도 정확히 추적
			performanceTracker.trackOrderMatched(orderId, false);
			log.error("Order processing failed: {}", orderId, e);
			return false;
		}
	}

	//---------------engine

	public OrderRequest parsingToOrderRequest(Order order) {
		return OrderRequest.from(order);
	}

	public CompanyOrderBook checkAndGetOrderBook(String companyCode) {
		if (!orderBookManager.hasOrderBook(companyCode)) {
			orderBookManager.createOrderBook(companyCode);
		}
		return orderBookManager.getOrderBook(companyCode);
	}

	public OrderQueue checkAndGetOrderQueue(CompanyOrderBook orderBook, OrderRequest request) {
		if (request.getType() == Type.BUY) {
			if (!orderBook.hasBuyQueue(request.getPrice())) {
				return orderBook.createBuyQueue(request.getPrice());
			}
			return orderBook.getBuyQueue(request.getPrice());
		} else {
			if (!orderBook.hasSellQueue(request.getPrice())) {
				return orderBook.createSellQueue(request.getPrice());
			}
			return orderBook.getSellQueue(request.getPrice());
		}
	}

	//파싱된 데이터 체크 -> 체결까지
	public boolean processNewOrder(OrderRequest orderRequest) {
		try {
			log.info("neworderstart");
			long startTime = System.nanoTime();
			log.info("큐 검사");
			// 주문북 및 큐 검증
			CompanyOrderBook orderBook = checkAndGetOrderBook(orderRequest.getCompanyCode());
			OrderQueue orderQueue = checkAndGetOrderQueue(orderBook, orderRequest);
			log.info("주문 추가");
			// 주문 추가
			boolean orderAdded = orderQueue.addOrder(orderRequest);
			log.info("매칭프로세스");
			// 매칭 프로세스 실행
			boolean matchingResult = processMatching(orderRequest.getCompanyCode());
			log.info("리턴 전 orderadd : {},matchingResult : {} ", orderAdded, matchingResult);
			// 주문 추가와 매칭 모두 성공해야 true 반환
			return orderAdded && matchingResult;
		} catch (Exception e) {
			// 예외 발생 시 false 반환
			log.error("Error processing order: {}", orderRequest, e);
			return false;
		}
	}

}
