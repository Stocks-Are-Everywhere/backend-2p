package org.scoula.backend.order.controller;

import org.scoula.backend.order.dto.ChartResponseDto;
import org.scoula.backend.order.service.TradeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chart")
@RequiredArgsConstructor
@Slf4j
public class ChartRestController {

	private final TradeHistoryService tradeHistoryService;

	@GetMapping("/{symbol}/history")
	public ResponseEntity<ChartResponseDto> getChartHistory(@PathVariable("symbol") String symbol) {
		try {
			ChartResponseDto response = tradeHistoryService.getChartHistory(symbol);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Failed to get chart history for symbol: {}", symbol, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
