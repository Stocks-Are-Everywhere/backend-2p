package org.scoula.backend.order.service;

import java.util.concurrent.ConcurrentHashMap;

import org.scoula.backend.order.controller.request.OrderRequest;
import org.scoula.backend.order.controller.response.OrderBookResponse;
import org.scoula.backend.order.controller.response.OrderSnapshotResponse;
import org.scoula.backend.order.controller.response.OrderSummaryResponse;
import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.dto.OrderDto;
import org.scoula.backend.order.repository.OrderRepositoryImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

	// 종목 코드를 키로 하는 주문들
	private final ConcurrentHashMap<String, OrderBookService> orderBooks = new ConcurrentHashMap<>();

	private final OrderRepositoryImpl orderRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 지정가 주문
	public void placeOrder(final OrderRequest request) {
		final Order order = new OrderDto(request).to();
		// 주문 처리
		final OrderBookResponse response = processOrder(order);
		// WebSocket으로 호가창 업데이트 브로드캐스트
		broadcastOrderBookUpdate(response.companyCode(), response);
	}

	// 주문 처리
	public OrderBookResponse processOrder(final Order order) {
		final OrderBookService orderBook = addOrderBook(order.getCompanyCode());
		orderBook.received(order);
		final OrderBookResponse response = orderBook.getBook();

		// 웹소켓 보내기
		broadcastOrderBookUpdate(response.companyCode(), response);
		return response;
	}

	// 종목별 주문장 생성, 이미 존재할 경우 반환
	public OrderBookService addOrderBook(final String companyCode) {
		return orderBooks.computeIfAbsent(companyCode, k -> new OrderBookService(companyCode));
	}

	// 주문 발생 시 호가창 업데이트 브로드캐스트
	public void broadcastOrderBookUpdate(final String code, final OrderBookResponse orderBook) {
		messagingTemplate.convertAndSend("/topic/orderbook/" + code, orderBook);
	}

	// 웹소켓 종목별 호가창 생성
	public OrderBookResponse getOrderBook(final String code) {
		final OrderBookService orderBook = addOrderBook(code);
		return orderBook.getBook();
	}

	// JSON 종목별 주문장 스냅샷 생성
	public OrderSnapshotResponse getSnapshot(final String companyCode) {
		final OrderBookService orderBook = addOrderBook(companyCode);
		return orderBook.getSnapshot();
	}

	// JSON 종목별 호가창 생성
	public OrderBookResponse getBook(final String companyCode) {
		final OrderBookService orderBook = addOrderBook(companyCode);
		return orderBook.getBook();
	}

	// JSON 종목별 주문 요약 생성
	public OrderSummaryResponse getSummary(final String companyCode) {
		final OrderBookService orderBook = addOrderBook(companyCode);
		return orderBook.getSummary();
	}

}
