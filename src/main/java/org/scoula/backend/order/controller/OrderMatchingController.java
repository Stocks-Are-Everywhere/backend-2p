// package org.scoula.backend.order.controller;
//
// import org.scoula.backend.order.domain.Type;
// import org.scoula.backend.order.service.multiqueue.OrderMatchingService;
// import org.scoula.backend.order.service.multiqueue.OrderRequest;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @RestController
// @RequestMapping("/api/orders")
// @RequiredArgsConstructor
// @Slf4j
// public class OrderMatchingController {
// 	private final OrderMatchingService orderMatchingService;
// 	private final MarketDataDummyGenerator dummyGenerator;
//
// 	@PostMapping("/test-run")
// 	public void runMatchingTest() {
// 		// 더미 데이터 생성 시작
// 		dummyGenerator.startGenerating();
// 		// 테스트 주문 생성 및 처리
// 		createTestOrders();
// 	}
//
// 	private void createTestOrders() {
// 		// 삼성전자 매수 주문
// 		OrderRequest buyOrder1 = OrderRequest.builder()
// 			.companyCode("005930")
// 			.type(Type.BUY)
// 			.price(70000)
// 			.quantity(50)
// 			.build();
//
// 		// 삼성전자 매도 주문
// 		OrderRequest sellOrder1 = OrderRequest.builder()
// 			.companyCode("005930")
// 			.type(Type.SELL)
// 			.price(70100)
// 			.quantity(30)
// 			.build();
//
// 		// SK하이닉스 매수 주문
// 		OrderRequest buyOrder2 = OrderRequest.builder()
// 			.companyCode("000660")
// 			.type(Type.BUY)
// 			.price(150000)
// 			.quantity(20)
// 			.build();
//
// 		// 주문 처리
// 		orderMatchingService.processOrder(buyOrder1);
// 		orderMatchingService.processOrder(sellOrder1);
// 		orderMatchingService.processOrder(buyOrder2);
// 	}
// }
