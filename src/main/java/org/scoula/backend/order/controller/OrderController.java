package org.scoula.backend.order.controller;

import org.scoula.backend.order.domain.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/test")
public class OrderController {

	@PostMapping("/order")
	public ResponseEntity<Order> PostOrder(Order order) {
		return ResponseEntity.ok(order);
	}

}
