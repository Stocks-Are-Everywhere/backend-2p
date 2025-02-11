package org.scoula.backend.order.service;

import org.scoula.backend.order.domain.Order;

public interface OrderService {

	Order createOrder();

	boolean validateOrder(Order order);

	Order createExecuteCondition(Order order);

	boolean validateExecuteCondition(Order order);
}
