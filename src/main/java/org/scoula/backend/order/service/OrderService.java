package org.scoula.backend.order.service;

import org.scoula.backend.order.domain.Order;

public interface OrderService {

	void ProcessMarketData(String parsingData);

	Boolean ProcessOrder(Order order);
}
