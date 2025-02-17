package org.scoula.backend.order.service.multiqueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.scoula.backend.order.domain.Type;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderBookManager {
	private final ConcurrentMap<String, CompanyOrderBook> orderBooks;

	public OrderBookManager() {
		this.orderBooks = new ConcurrentHashMap<>();
		initializeDummyOrderBooks();
		log.info("OrderBookManager initialized orderbook");
	}

	private void initializeDummyOrderBooks() {
		String initialCompanyCode = "000000";
		createOrderBook(initialCompanyCode);
		// 더미 OrderBook에 대한 기본 가격 큐 초기화
		initializeDummyQueues(initialCompanyCode);
	}

	public void createOrderBook(String companyCode) {
		orderBooks.computeIfAbsent(companyCode, code -> {
			CompanyOrderBook newBook = new CompanyOrderBook();
			log.info("Created new order book for company: {}", code);
			return newBook;
		});
		initializeDummyQueues(companyCode);
	}

	private void initializeDummyQueues(String companyCode) {
		CompanyOrderBook orderBook = orderBooks.get(companyCode);
		int dummyPrice = 10000;

		OrderQueue buyQueue = orderBook.createBuyQueue(dummyPrice);
		OrderQueue sellQueue = orderBook.createSellQueue(dummyPrice);

		OrderRequest dummyBuyOrder = OrderRequest.createDummy(companyCode, Type.BUY, dummyPrice);
		OrderRequest dummySellOrder = OrderRequest.createDummy(companyCode, Type.SELL, dummyPrice);
		buyQueue.addOrder(dummyBuyOrder);
		sellQueue.addOrder(dummySellOrder);
		log.info("Initialized default buy/sell queues for company: {}", companyCode);
	}

	public CompanyOrderBook getOrderBook(String companyCode) {
		return orderBooks.get(companyCode);
	}

	public boolean hasOrderBook(String companyCode) {
		return orderBooks.containsKey(companyCode);
	}
}



