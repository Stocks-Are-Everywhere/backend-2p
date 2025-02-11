package org.scoula.backend.order.service;

import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataHandler {

	// 조건 형성을 위한 marketdata 파싱
	@Getter
	@Setter
	@Builder
	public static class MarketData {
		private String companyCode;
		private String time;
		private int currentPrice;
		private int quantity;
		private double priceChange;
		private double averagePrice;
		private int buyCount;
		private int sellCount;
		private long accumulatedVolume;

		// marketdata 파싱 메서드
		private MarketData parsePayload(String payload) {
			try {
				String[] fields = payload.split("\\|");

				String[] stockData = fields[3].split("\\^");
				return MarketData.builder()
					.companyCode(stockData[0])
					.time(stockData[1])
					.currentPrice(Integer.parseInt(stockData[2]))
					.quantity(Integer.parseInt(stockData[3]))
					.priceChange(Double.parseDouble(stockData[4]))
					.averagePrice(Double.parseDouble(stockData[6]))
					.buyCount(Integer.parseInt(stockData[14]))
					.sellCount(Integer.parseInt(stockData[15]))
					.accumulatedVolume(Long.parseLong(stockData[12]))
					.build();
			} catch (Exception e) {
				log.error("Failed to parse market data payload: {}", payload, e);

			}
			return null;
		}
	}

}
