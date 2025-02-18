package org.scoula.backend.order.controller;

import org.scoula.backend.order.controller.request.OrderRequest;
import org.scoula.backend.order.controller.response.OrderBookResponse;
import org.scoula.backend.order.controller.response.OrderSnapshotResponse;
import org.scoula.backend.order.controller.response.OrderSummaryResponse;
import org.scoula.backend.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<Void> received(@RequestBody final OrderRequest request) {
		orderService.placeOrder(request);
		return ResponseEntity.ok().build();
	}

	// JSON 종목별 주문장 스냅샷 생성
	@GetMapping("/snapshot")
	public ResponseEntity<OrderSnapshotResponse> getSnapshot(@RequestParam("code") final String companyCode) {
		return ResponseEntity.ok(orderService.getSnapshot(companyCode));
	}

	// JSON 종목별 호가창 생성
	@GetMapping("/book")
	public ResponseEntity<OrderBookResponse> getBook(@RequestParam("code") final String companyCode) {
		return ResponseEntity.ok(orderService.getBook(companyCode));
	}

	// JSON 종목별 주문 요약 생성
	@GetMapping("/summary")
	public ResponseEntity<OrderSummaryResponse> getSummary(@RequestParam("code") final String companyCode) {
		return ResponseEntity.ok(orderService.getSummary(companyCode));
	}

}
