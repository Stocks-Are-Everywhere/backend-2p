package org.scoula.backend.order.controller;

import org.scoula.backend.order.dto.ChartResponseDto;
import org.scoula.backend.order.service.TradeHistoryService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chart")
public class ChartController {

	private final TradeHistoryService tradeHistoryService;
	private final SimpMessagingTemplate messagingTemplate;

	// // charthistory 초기 값 호출
	// @GetMapping("/{symbol}/history")
	// public ResponseEntity<ChartResponseDto> getChartHistory(@PathVariable("symbol") String symbol) {
	// 	try {
	// 		ChartResponseDto response = tradeHistoryService.getChartHistory(symbol);
	// 		return ResponseEntity.ok(response);
	// 	} catch (Exception e) {
	// 		log.error("Failed to get chart history for symbol: {}", symbol, e);
	// 		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	// 	}
	// }

	// 차트 웹소켓
	@Scheduled(fixedRate = 15000) // 15초마다 새로운 캔들 생성
	public void sendCandleUpdates() {
		try {
			// 새로운 캔들 생성 및 기존 캔들 업데이트
			tradeHistoryService.updateCandles("005930");

			// 업데이트된 캔들 데이터 전송
			ChartResponseDto candleData = tradeHistoryService.getChartHistory("005930");
			messagingTemplate.convertAndSend("/topic/candle/" + "005930", candleData);

			log.debug("Sent candle update for symbol: {}, candles size: {}",
				"005930", candleData.getCandles().size());
		} catch (Exception e) {
			log.error("Failed to send candle update for symbol: {}", "005930", e);
		}
	}

}
