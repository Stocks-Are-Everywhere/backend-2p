package org.scoula.backend.order.controller;

import org.scoula.backend.order.controller.response.OrderBookResponse;
import org.scoula.backend.order.service.OrderService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ws")
@RequiredArgsConstructor
public class OrderBookWebSocketController {
	
	private final OrderService orderService;

	// 클라이언트가 특정 종목 구독 시 초기 데이터 전송
	@SubscribeMapping("/topic/orderbook/{code}")
	public OrderBookResponse subscribeOrderBook(@DestinationVariable final String code) {
		return orderService.getOrderBook(code);
	}
}
