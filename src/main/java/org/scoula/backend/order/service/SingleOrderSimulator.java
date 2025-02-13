package org.scoula.backend.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.scoula.backend.order.controller.response.OrderBookResponse;
import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SingleOrderSimulator {
	private final OrderManager orderManager;
	private final Random random = new Random();
	private final ScheduledExecutorService executor;

	// 시뮬레이션 설정
	private static final String CODE = "005930"; // 삼성전자
	private static final BigDecimal BASE_PRICE = new BigDecimal("74000");
	private static final Integer PRICE_RANGE = 1000; // 기준가 +-1000원
	private static final Integer SECONDS_BETWEEN_ORDERS = 1;
	private static final Integer ORDER_INTERVAL = SECONDS_BETWEEN_ORDERS * 1000; // n초로 변환

	public SingleOrderSimulator(final OrderManager orderManager) {
		this.orderManager = orderManager;
		this.executor = Executors.newSingleThreadScheduledExecutor();
	}

	// 시뮬레이션 시작
	public void startSimulation() {
		executor.scheduleAtFixedRate(
				this::generateRandomOrder,
				0,
				ORDER_INTERVAL,
				TimeUnit.MICROSECONDS
		);
	}

	// 랜덤 주문 생성
	private void generateRandomOrder() {
		try {
			final Order order = createRandomOrder();
			orderManager.processOrder(order);
			logOrderBook(order.getCompanyCode());
		} catch (Exception e) {
			log.error("Error generating order", e);
		}
	}

	private Order createRandomOrder() {
		final Type type = random.nextBoolean() ? Type.BUY : Type.SELL;
		final BigDecimal price = generateRandomPrice();
		final Integer quantity = generateRandomQuantity();

		return Order.builder()
				.companyCode(CODE)
				.type(type)
				.price(price)
				.totalQuantity(quantity)
				.remainingQuantity(quantity)
				.timestamp(LocalDateTime.now())
				.build();

	}

	private BigDecimal generateRandomPrice() {
		Integer priceOffset = random.nextInt(PRICE_RANGE * 2) - PRICE_RANGE;
		return BASE_PRICE.add(new BigDecimal(priceOffset));
	}

	private Integer generateRandomQuantity() {
		return random.nextInt(1000) + 100; // 100~11000주
	}

	private void logOrderBook(final String code) {
		final OrderBook orderBook = orderManager.getOrderBook(code);
		final OrderBookResponse response = orderBook.getBook();
		log.info("\n{}", formatOrderBook(response));
	}

	// 호가창 데이터를 문자열로 표현하는 메서드 추가 (디버깅/로깅용)
	public static String formatOrderBook(OrderBookResponse response) {
		StringBuilder sb = new StringBuilder();
		sb.append("=== ").append(response.companyCode()).append(" 호가창 ===\n");

		// 매도호가 (역순)
		sb.append("[매도호가]\n");
		response.askLevels().forEach(level ->
				sb.append(String.format("%,8d  %,7d주  %d건\n",
						level.price().intValue(),
						level.quantity(),
						level.orderCount()
				))
		);

		// 구분선
		sb.append("-".repeat(30)).append("\n");

		// 매수호가
		sb.append("[매수호가]\n");
		response.bidLevels().forEach(level ->
				sb.append(String.format("%,8d  %,7d주  %d건\n",
						level.price().intValue(),
						level.quantity(),
						level.orderCount()
				))
		);
		return sb.toString();
	}

	// 시뮬레이션 중지
	public void stopSimulation() {
		executor.shutdown();
	}
}
