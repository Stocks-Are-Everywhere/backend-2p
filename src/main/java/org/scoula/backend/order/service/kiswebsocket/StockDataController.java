package org.scoula.backend.order.service.kiswebsocket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockDataController {
	private final StockDataService stockDataService;

	@PostMapping("/subscribe/{code}")
	public ResponseEntity<String> subscribeStock(@PathVariable(name = "code") String code) {
		try {
			stockDataService.startStockDataStream(code);
			return ResponseEntity.ok("Connected to stock: " + code);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Connection failed: " + e.getMessage());
		}
	}

	@PostMapping("/unsubscribe/{code}")
	public ResponseEntity<String> unsubscribeStock(@PathVariable(name = "code") String code) {
		try {
			stockDataService.stopStockDataStream(code);
			return ResponseEntity.ok("Disconnected from stock: " + code);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Disconnection failed: " + e.getMessage());
		}
	}
}
