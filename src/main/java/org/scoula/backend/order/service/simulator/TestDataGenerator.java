package org.scoula.backend.order.service.simulator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.scoula.backend.order.service.OrderServiceImpl;
import org.scoula.backend.order.service.multiqueue.MarketCondition;
import org.scoula.backend.order.service.multiqueue.MarketStateCache;
import org.scoula.backend.order.service.multiqueue.OrderBookManager;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TestDataGenerator {
	// 테스트 데이터 생성을 위한 구성 파라미터
	// ********************MergeRequired**************************
	private static final String[] COMPANY_CODES = {"005930", "000660", "035420"};

	// 시세 데이터 생성 범위 설정
	private static final int MIN_CURRENT_PRICE = 50_000;
	private static final int MAX_CURRENT_PRICE = 51_000;

	// 호가 변동 범위 설정
	private static final int PRICE_VARIATION_RANGE = 10;

	// 주문 수량 범위 설정
	private static final int MIN_ORDER_QUANTITY = 1;
	private static final int MAX_ORDER_QUANTITY = 100;

	// 주문 가격 변동 비율
	// private static final double ORDER_PRICE_VARIATION_RATIO = 0.1;

	private final OrderServiceImpl orderService;
	private final OrderBookManager orderBookManager;
	private final Random random;
	private final MarketStateCache marketStateCache;

	public TestDataGenerator(OrderServiceImpl orderService, MarketStateCache marketStateCache,
		OrderBookManager orderBookManager) {
		this.orderService = orderService;
		this.marketStateCache = marketStateCache;
		this.orderBookManager = orderBookManager;
		this.random = new Random();
	}

	/**
	 * 초기 마켓 조건 설정
	 * - 각 회사코드에 대해 더미 시세 데이터 생성
	 * - 마켓 상태 캐시 업데이트
	 * - 주문북 초기화
	 */
	public void initializeMarketConditions() {
		for (String companyCode : COMPANY_CODES) {
			// 더미 시세 데이터 생성
			String dummyMarketData = generateDummyMarketData(companyCode);

			// 마켓 컨디션 파싱 및 캐시 업데이트
			MarketCondition condition = MarketCondition.parse(dummyMarketData);
			marketStateCache.updateCondition(condition);

			// 주문북 초기화
			orderBookManager.createOrderBook(companyCode);

			log.info("Initialized market condition for company: {}", companyCode);
		}
	}

	/**
	 * 더미 시세 데이터 생성
	 * @param companyCode 회사 코드
	 * @return 생성된 더미 시세 데이터 문자열
	 */
	private String generateDummyMarketData(String companyCode) {
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append(companyCode).append("^");

		// 현재가 10 단위로 반올림
		int currentPrice = (int)Math.round(
			random.nextInt(MAX_CURRENT_PRICE - MIN_CURRENT_PRICE + 1) + MIN_CURRENT_PRICE / 10.0
		) * 10;

		dataBuilder.append(currentPrice).append("^");

		// 추가 더미 데이터 생성 (43개 필드)
		for (int i = 0; i < 41; i++) {
			dataBuilder.append(generateRealisticPrice(currentPrice)).append("^");
		}

		String dummyData = dataBuilder.toString();
		log.info("Generated dummy market data: {}", dummyData);
		return dummyData;
	}

	/*
	 * 현실적인 호가 생성 (10 단위 변동)
	 * @param basePrice 기준 가격
	 * @return 생성된 호가
	 */
	private int generateRealisticPrice(int basePrice) {
		// 10 단위로 반올림 및 변동
		int variation = (random.nextInt(2 * PRICE_VARIATION_RANGE) - PRICE_VARIATION_RANGE) / 10 * 10;
		return basePrice + variation;
	}

	/**
	 * 테스트 주문 생성
	 * @param numOrders 생성할 주문 수
	 * @return 생성된 주문 리스트
	 */
	public List<Order> generateTestOrders(int numOrders) {
		// 마켓 컨디션 초기화
		initializeMarketConditions();

		List<Order> orders = new ArrayList<>();

		for (int i = 0; i < numOrders; i++) {
			// 랜덤 회사 코드 선택
			// ********************MergeRequired**************************
			String companyCode = COMPANY_CODES[random.nextInt(COMPANY_CODES.length)];

			// ********************MergeRequired**************************
			// 해당 회사의 현재 마켓 컨디션 조회
			MarketCondition condition = orderService.getCondition(companyCode);

			// 주문 생성
			Order order = Order.builder()
				.id((long)(100 + i))
				.companyCode(companyCode)
				.type(random.nextBoolean() ? Type.BUY : Type.SELL)
				.price(generateRealisticOrderPrice(condition.getCurrentPrice()))
				.totalQuantity(random.nextInt(MAX_ORDER_QUANTITY - MIN_ORDER_QUANTITY + 1) + MIN_ORDER_QUANTITY)
				.createdDateTime(LocalDateTime.now())
				.build();

			orders.add(order);
		}
		return orders;
	}

	/**
	 * 현실적인 주문 가격 생성
	 * @param basePrice 기준 가격
	 * @return 생성된 주문 가격
	 */
	/**
	 * 현실적인 주문 가격 생성 (10 단위 기반)
	 * @param basePrice 기준 가격
	 * @return 생성된 주문 가격
	 */

	// ********************MergeRequired**************************
	private int generateRealisticOrderPrice(int basePrice) {
		// 10 단위 기반 가격 변동
		int minPrice = (int)Math.round(basePrice * 0.9 / 10) * 10;
		int maxPrice = (int)Math.round(basePrice * 1.1 / 10) * 10;

		// 10 단위 랜덤 가격 생성
		int priceRange = (maxPrice - minPrice) / 10 + 1;
		int randomOffset = random.nextInt(priceRange);

		return minPrice + (randomOffset * 10);
	}
}

