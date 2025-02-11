package org.scoula.backend.order.controller;

import org.scoula.backend.order.service.KafkaProducerService;
import org.scoula.backend.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final KafkaProducerService kafkaProducerService;

	// @PostMapping
	// public ResponseEntity<Void> received(@RequestBody final OrderRequest request) {
	// 	orderService.received(request);
	// 	return ResponseEntity.ok().build();
	// }

	@PostMapping("/kafka")
	public ResponseEntity<Void> sendMassage(
			@RequestBody String message) {
		this.kafkaProducerService.sendMessage(message);
		return ResponseEntity.ok().build();
	}

}

