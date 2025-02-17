package org.scoula.backend.order.service.multiqueue;

import java.time.LocalDateTime;
import java.util.Arrays;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketCondition {
	private String companyCode;        // 종목 코드
	private int currentPrice;          // 현재가
	private int[] buyPrices;          // 매수 호가 배열
	private int[] sellPrices;         // 매도 호가 배열
	private int[] buyQuantities;      // 매수 잔량 배열
	private int[] sellQuantities;     // 매도 잔량 배열
	private int matchableQuantity;    // 체결 가능 수량
	private LocalDateTime timestamp;   // 시간

	// 시세 데이터 파싱
	public static MarketCondition parse(String data) {
		String[] fields = data.split("\\^");

		return MarketCondition.builder()
			.companyCode(fields[0])
			.currentPrice(Integer.parseInt(fields[3]))
			.buyPrices(parsePrices(fields, 3, 13))    // 매수 호가
			.sellPrices(parsePrices(fields, 13, 23))  // 매도 호가
			.buyQuantities(parseQuantities(fields, 23, 33))  // 매수 잔량
			.sellQuantities(parseQuantities(fields, 33, 43)) // 매도 잔량
			.matchableQuantity(calculateMatchableQuantity(fields))
			.timestamp(LocalDateTime.now())
			.build();
	}

	private static int[] parsePrices(String[] fields, int start, int end) {
		return Arrays.stream(Arrays.copyOfRange(fields, start, end))
			.mapToInt(Integer::parseInt)
			.toArray();
	}

	private static int[] parseQuantities(String[] fields, int start, int end) {
		return Arrays.stream(Arrays.copyOfRange(fields, start, end))
			.mapToInt(Integer::parseInt)
			.toArray();
	}

	private static int calculateMatchableQuantity(String[] fields) {
		// 체결 가능 수량 계산 로직
		return Math.min(
			Arrays.stream(parseQuantities(fields, 23, 33)).sum(),
			Arrays.stream(parseQuantities(fields, 33, 43)).sum()
		);
	}

	// 매수 주문 체결 가능 여부 확인
	public boolean isMatchableBuy(OrderRequest order) {
		return order.getPrice() >= currentPrice &&
			order.getQuantity() <= matchableQuantity;
	}

	// 매도 주문 체결 가능 여부 확인
	public boolean isMatchableSell(OrderRequest order) {
		return order.getPrice() <= currentPrice &&
			order.getQuantity() <= matchableQuantity;
	}
}

