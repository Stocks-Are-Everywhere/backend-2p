package org.scoula.backend.order.service;

import org.scoula.backend.order.controller.request.OrderRequest;
import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.dto.OrderDto;
import org.scoula.backend.order.repository.OrderRepositoryImpl;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

	private final OrderRepositoryImpl orderRepository;

	public void received(final OrderRequest request) {
		final Order order = new OrderDto(request).to();

	}

	public void matching() {

	}

}
