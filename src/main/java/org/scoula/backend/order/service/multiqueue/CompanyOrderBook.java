package org.scoula.backend.order.service.multiqueue;

import java.util.Collections;
import java.util.TreeMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CompanyOrderBook {
	private final TreeMap<Integer, OrderQueue> buyOrders = new TreeMap<>(Collections.reverseOrder());
	private final TreeMap<Integer, OrderQueue> sellOrders = new TreeMap<>();

	// 가격별 Buy 큐 존재 여부 확인
	public boolean hasBuyQueue(int price) {
		return buyOrders.containsKey(price);
	}

	// 가격별 Sell 큐 존재 여부 확인
	public boolean hasSellQueue(int price) {
		return sellOrders.containsKey(price);
	}

	// Buy 큐 생성
	public OrderQueue createBuyQueue(int price) {
		return buyOrders.computeIfAbsent(price, k -> {
			log.info("Created new buy order queue at price: {}", price);
			return new OrderQueue();
		});
	}

	// Sell 큐 생성
	public OrderQueue createSellQueue(int price) {
		return sellOrders.computeIfAbsent(price, k -> {
			log.info("Created new sell order queue at price: {}", price);
			return new OrderQueue();
		});
	}

	// 기존 큐 조회
	public OrderQueue getBuyQueue(int price) {
		return buyOrders.get(price);
	}

	public OrderQueue getSellQueue(int price) {
		return sellOrders.get(price);
	}
}


