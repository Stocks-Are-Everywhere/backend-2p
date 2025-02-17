package org.scoula.backend.order.service.multiqueue;

import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderQueue {
	private final ConcurrentLinkedQueue<OrderRequest> orders = new ConcurrentLinkedQueue<>();

	public boolean addOrder(OrderRequest orderRequest) {
		try {
			// 기존 로직
			orders.add(orderRequest);
			return true;
		} catch (Exception e) {
			log.error("Failed to add order to queue", e);
			return false;
		}
	}

	public OrderRequest peek() {
		return orders.peek();
	}

	public OrderRequest poll() {
		return orders.poll();
	}

	public boolean isEmpty() {
		return orders.isEmpty();
	}
}
