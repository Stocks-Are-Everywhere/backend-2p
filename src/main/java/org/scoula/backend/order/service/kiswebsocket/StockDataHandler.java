package org.scoula.backend.order.service.kiswebsocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockDataHandler {
	private final SimpMessagingTemplate messagingTemplate;

	public void handleStockData(String rawData) {
		try {
			StockData stockData = parseKisData(rawData);
			sendToClients(stockData);
		} catch (Exception e) {
			log.error("Error handling stock data: {}", e.getMessage());
			log.error("Stack trace:", rawData);
		}
	}

	private StockData parseKisData(String rawData) {
		try {
			String[] sections = rawData.split("\\|");
			if (sections.length < 4) {
				log.error("Invalid data format: missing sections | Raw data: {}", rawData);
				throw new IllegalArgumentException("Invalid data format: missing sections");
			}

			String[] fields = sections[3].split("\\^");

			StockData data = new StockData();
			data.setTime(fields[1]);                              // 체결시각
			data.setCurrentPrice(Double.parseDouble(fields[2]));  // 현재가
			data.setVolume(Long.parseLong(fields[12]));          // 체결량
			data.setAccVolume(Long.parseLong(fields[13]));       // 누적거래량

			return data;
		} catch (Exception e) {
			log.error("Failed to parse data: {} | Error: {}", rawData, e.getMessage());
			throw e;
		}
	}

	private void sendToClients(StockData data) {
		messagingTemplate.convertAndSend("/topic/stockdata", data);
	}
}


