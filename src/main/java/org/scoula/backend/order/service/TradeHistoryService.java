package org.scoula.backend.order.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.scoula.backend.order.controller.response.TradeHistoryResponse;
import org.scoula.backend.order.domain.TradeHistory;
import org.scoula.backend.order.dto.CandleDto;
import org.scoula.backend.order.dto.ChartResponseDto;
import org.scoula.backend.order.dto.ChartUpdateDto;
import org.scoula.backend.order.repository.TradeHistoryRepositoryImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {
	private final TradeHistoryRepositoryImpl tradeHistoryRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 종목코드별 최근 거래 내역을 메모리에 유지
	private final Map<String, ConcurrentLinkedQueue<TradeHistory>> recentTradesMap = new ConcurrentHashMap<>();
	// 종목코드별 캔들 데이터를 메모리에 유지
	private final Map<String, List<CandleDto>> candleMap = new ConcurrentHashMap<>();

	private static final int MAX_TRADE_HISTORY = 1000; // 종목당 최대 보관 거래 수
	private static final int CANDLE_KEEP_NUMBER = 30; // 캔들 데이터 보관 개수

	@PostConstruct
	public void initialize() {
		// 초기 데이터 구조 설정
		recentTradesMap.put("005930", new ConcurrentLinkedQueue<>());
		candleMap.put("005930", new ArrayList<>());

		// 초기 캔들 생성 (30개)
		createInitialCandles();
	}

	private void createInitialCandles() {
		long now = Instant.now().getEpochSecond();
		long baseTime = now - (now % 15); // 15초 간격

		for (String symbol : recentTradesMap.keySet()) {
			List<CandleDto> candles = new ArrayList<>();
			double basePrice = 70000; // 초기 가격

			// 최근 30개의 캔들 생성
			for (int i = 1; i >= 0; i--) {
				long candleTime = baseTime - (i * 15);
				candles.add(CandleDto.builder()
						.time(candleTime)
						.open(basePrice)
						.high(basePrice)
						.low(basePrice)
						.close(basePrice)
						.volume(0)
						.build());

			}

			candleMap.put(symbol, candles);
		}
	}

	public void updateCandles(String symbol) {
		List<CandleDto> existingCandles = candleMap.get(symbol);
		if (existingCandles == null) {
			existingCandles = new ArrayList<>();
		}

		long now = Instant.now().getEpochSecond();
		long currentCandleTime = now - (now % 15);

		// 현재 진행 중인 캔들이 없거나 새로운 캔들을 만들어야 하는 경우
		// existingCandles.get(existingCandles.size() - 1).getTime() < currentCandleTime
		if (existingCandles.isEmpty()) {

			// 새 캔들 추가
			CandleDto newCandle = CandleDto.builder()
					.time(currentCandleTime)
					.open(getLastPrice(symbol))
					.high(getLastPrice(symbol))
					.low(getLastPrice(symbol))
					.close(getLastPrice(symbol))
					.volume(0)
					.build();

			existingCandles.add(newCandle);

			// 최대 30개 유지
			if (existingCandles.size() > CANDLE_KEEP_NUMBER) {
				existingCandles = existingCandles.subList(
						existingCandles.size() - CANDLE_KEEP_NUMBER,
						existingCandles.size()
				);
			}
		} else {
			CandleDto prevCandle = existingCandles.get(existingCandles.size() - 1);
			CandleDto nextCandle = CandleDto.builder()
					.time(currentCandleTime)
					.open(prevCandle.getClose())
					.high(prevCandle.getClose())
					.low(prevCandle.getClose())
					.close(prevCandle.getClose())
					.volume(0)
					.build();

			existingCandles.add(nextCandle);

			// 최대 30개 유지
			if (existingCandles.size() > CANDLE_KEEP_NUMBER) {
				existingCandles = existingCandles.subList(
						existingCandles.size() - CANDLE_KEEP_NUMBER,
						existingCandles.size()
				);
			}
		}

		candleMap.put(symbol, existingCandles);
	}

	private double getLastPrice(String symbol) {
		ConcurrentLinkedQueue<TradeHistory> trades = recentTradesMap.get(symbol);
		if (trades == null || trades.isEmpty()) {
			return 70000; // 기본 가격
		}
		return trades.peek().getPrice().doubleValue();
	}

	public Optional<TradeHistory> getLastTrade(String companyCode) {
		Queue<TradeHistory> trades = recentTradesMap.get(companyCode);
		return trades == null || trades.isEmpty() ?
				Optional.empty() : Optional.of(trades.peek());
	}

	public void saveTradeHistory(TradeHistoryResponse tradeHistoryResponse) {
		TradeHistory tradeHistory = convertToEntity(tradeHistoryResponse);

		// DB 저장
		tradeHistoryRepository.save(tradeHistory);

		// 메모리에 저장
		ConcurrentLinkedQueue<TradeHistory> trades =
				recentTradesMap.computeIfAbsent(tradeHistory.getCompanyCode(),
						k -> new ConcurrentLinkedQueue<>());

		trades.offer(tradeHistory);

		// 최대 개수 유지
		while (trades.size() > MAX_TRADE_HISTORY) {
			trades.poll();
		}

		// 현재 캔들 업데이트
		List<CandleDto> candles = candleMap.get(tradeHistory.getCompanyCode());
		if (candles != null && !candles.isEmpty()) {
			CandleDto currentCandle = candles.get(candles.size() - 1);

			CandleDto updatedCandle = CandleDto.builder()
					.time(currentCandle.getTime())
					.open(currentCandle.getClose())
					.high(Math.max(currentCandle.getHigh(), tradeHistory.getPrice().doubleValue()))
					.low(Math.min(currentCandle.getLow(), tradeHistory.getPrice().doubleValue()))
					.close(tradeHistory.getPrice().doubleValue())
					.volume(currentCandle.getVolume() + tradeHistory.getQuantity().intValue())
					.build();

			candles.set(candles.size() - 1, updatedCandle);
			candleMap.put(tradeHistory.getCompanyCode(), candles);
		}

		// WebSocket으로 실시간 데이터 전송
		ChartUpdateDto updateDto = ChartUpdateDto.builder()
				.price(tradeHistory.getPrice().doubleValue())
				.volume(tradeHistory.getQuantity().intValue())
				.build();
		messagingTemplate.convertAndSend("/topic/chart/" + tradeHistory.getCompanyCode(), updateDto);
	}

	public ChartResponseDto getChartHistory(String symbol) {
		List<CandleDto> candles = candleMap.getOrDefault(symbol, new ArrayList<>());
		return ChartResponseDto.builder()
				.candles(new ArrayList<>(candles))  // 방어적 복사
				.build();
	}

	// @Scheduled(fixedRate = 15000) // 15초마다 실행
	// public void scheduledCandleUpdate() {
	// 	recentTradesMap.keySet().forEach(symbol -> {
	// 		updateCandles(symbol);
	//
	// 		// WebSocket으로 캔들 데이터 전송
	// 		ChartResponseDto candleData = getChartHistory(symbol);
	// 		messagingTemplate.convertAndSend("/topic/candle/" + symbol, candleData);
	// 	});
	// }

	/// //////
	public List<TradeHistoryResponse> getTradeHistory() {
		List<TradeHistory> tradeHistories = tradeHistoryRepository.getTradeHistory();
		return tradeHistories.stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
	}

	private TradeHistory convertToEntity(TradeHistoryResponse dto) {
		return TradeHistory.builder()
				.id(dto.id())
				.companyCode(dto.companyCode())
				.sellOrderId(dto.sellOrderId())
				.buyOrderId(dto.buyOrderId())
				.quantity(dto.quantity())  // Note: There's a typo in the DTO field name
				.price(dto.price())
				.tradeTime(dto.tradeTime())
				.build();
	}

	private TradeHistoryResponse convertToDto(TradeHistory entity) {
		return TradeHistoryResponse.builder()
				.id(entity.getId())
				.companyCode(entity.getCompanyCode())
				.sellOrderId(entity.getSellOrderId())
				.buyOrderId(entity.getBuyOrderId())
				.quantity(entity.getQuantity())  // Note: Keeping the typo to match the DTO
				.price(entity.getPrice())
				.tradeTime(entity.getTradeTime())
				.build();
	}
}

