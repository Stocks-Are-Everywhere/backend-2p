package org.scoula.backend.order.service.simulator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.scoula.backend.order.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiOrderSimulator {
	private final OrderService orderService;
	private volatile Boolean isRunning = false;
	// private final Random random = new Random(); // 아마, 교착 상태 발생 가능성

	// 스레드 풀 설정
	private final ExecutorService orderGeneratorPool;
	// 주문 큐 추가
	// private final BlockingQueue<Order> orderQueue;

	// 시뮬레이션 설정
	private static final String CODE = "005930"; // 삼성전자
	private static final BigDecimal BASE_PRICE = new BigDecimal("74000");
	private static final Integer PRICE_RANGE = 11400; // 기준가 +-1000원
	private static final Integer NUMBER_OF_THREADS = 500; // 주문 생성 스레드 수
	private static final Integer MIN_INTERVAL = 100; // 최소 대기 시간
	private static final Integer MAX_INTERVAL = 300; // 최대 대기 시간

	public MultiOrderSimulator(final OrderService orderService) {
		this.orderService = orderService;
		this.orderGeneratorPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		// this.orderQueue = new LinkedBlockingDeque<>(1000); // 큐 크기 제한
	}

	// 시뮬레이션 시작
	public void startSimulation() {
		isRunning = true;
		// 각 사용자별로 주문 생성 스레드 시작
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			// final String userId = "USER_" + i;
			orderGeneratorPool.submit(this::generateRandomOrder);
		}
	}

	// 랜덤 주문 생성
	private void generateRandomOrder() {
		// try {
		// 	final Order order = createRandomOrder();
		// 	orderManager.processOrder(order);
		// 	logOrderBook(order.getCompanyCode());
		// 	// Thread.sleep(ORDER_INTERVAL + random.nextInt(1000));
		// 	// 다음 주문까지 대기
		// 	// Thread.sleep(1);
		// } catch (Exception e) {
		// 	log.error("Error generating order", e);
		// }

		final Random random = new Random();
		while (isRunning) {
			try {
				// 주문 생성 및 처리
				final Order order = createRandomOrder(random);
				orderService.processOrder(order);

				// 랜덤 시간 간격으로 대기
				final Integer waitTime = random.nextInt(MAX_INTERVAL - MIN_INTERVAL) + MIN_INTERVAL;
				Thread.sleep(waitTime); // 0.1 - 0.3초 대기
			} catch (InterruptedException e) {
				log.info("simulation interrupted");
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				log.info("simulation error");
				// 에러 발생 시 잠시 대기 후 재시도
				try {
					Thread.sleep(1000); // 1초 대기
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}

		}
	}

	private Order createRandomOrder(final Random random) {
		final Type type = random.nextBoolean() ? Type.BUY : Type.SELL;
		final BigDecimal price = generateRandomPrice(random);
		final Integer quantity = generateRandomQuantity(random);

		return Order.builder()
				.companyCode(CODE)
				.type(type)
				.price(price)
				.totalQuantity(quantity)
				.remainingQuantity(quantity)
				.timestamp(LocalDateTime.now())
				.build();

	}

	private BigDecimal generateRandomPrice(final Random random) {
		Integer priceOffset = random.nextInt(PRICE_RANGE * 2) - PRICE_RANGE;
		return BASE_PRICE.add(new BigDecimal(priceOffset));
	}

	private Integer generateRandomQuantity(final Random random) {
		return random.nextInt(1000) + 100; // 100~11000주
	}

	// 시뮬레이션 중지
	// public void stopSimulation() {
	// 	executors.forEach(executor -> {
	// 		try {
	// 			executor.shutdown();
	// 			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
	// 				executor.shutdownNow();
	// 			}
	// 		} catch (InterruptedException e) {
	// 			executor.shutdownNow();
	// 			Thread.currentThread().interrupt();
	// 		}
	// 	});
	// }
}
