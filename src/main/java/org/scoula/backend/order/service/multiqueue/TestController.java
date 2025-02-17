package org.scoula.backend.order.service.multiqueue;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.service.OrderServiceImpl;
import org.scoula.backend.order.service.multiqueue.monitoring.PerformanceMetrics;
import org.scoula.backend.order.service.multiqueue.monitoring.PerformanceTracker;
import org.scoula.backend.order.service.multiqueue.monitoring.TestScenario;
import org.scoula.backend.order.service.simulator.TestDataGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {
	// 테스트 설정 상수
	private static final int DEFAULT_ORDER_COUNT = 100;
	private static final int DEFAULT_CONCURRENT_THREADS = 10;
	private static final int MAX_ORDER_COUNT = 100;
	private static final int MAX_CONCURRENT_THREADS = 100;

	private final OrderServiceImpl orderService;
	private final TestDataGenerator testDataGenerator;
	private final PerformanceTracker performanceTracker;
	private final MarketStateCache marketStateCache;

	/**
	 * 성능 테스트 엔드포인트
	 *
	 * @param numOrders 테스트할 주문 수 (기본값 1000, 최대 10000)
	 * @param concurrentThreads 동시 실행 스레드 수 (기본값 10, 최대 100)
	 * @return 성능 메트릭스
	 */
	@PostMapping("/performance")
	public ResponseEntity<PerformanceMetrics> runPerformanceTest(
		@RequestParam(defaultValue = "1000") int numOrders,
		@RequestParam(defaultValue = "10") int concurrentThreads
	) throws InterruptedException {
		// 입력값 검증
		log.info("테스트 시작 - 주문 수: {}, 동시 처리 스레드: {}", numOrders, concurrentThreads);

		long startTime = System.currentTimeMillis();
		numOrders = Math.min(numOrders, MAX_ORDER_COUNT);
		concurrentThreads = Math.min(concurrentThreads, MAX_CONCURRENT_THREADS);

		// 스레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreads);

		// 테스트 데이터 생성
		List<Order> orders = testDataGenerator.generateTestOrders(numOrders);

		//수정 필요 ->service 꼴
		orders.forEach(order ->
			CompletableFuture.runAsync(() -> {
				try {
					// 주문 처리 로직
					processOrder(order);

					// 임의의 대기 시간 추가 (예: 1-3초)
					Thread.sleep(new Random().nextInt(2000) + 1000);
				} catch (InterruptedException e) {
					// InterruptedException 처리
					Thread.currentThread().interrupt();
					log.error("Order processing interrupted", e);
				} catch (Exception e) {
					log.error("Error processing order", e);
				}
			}, executorService)
		);

		// 스레드 풀 종료 및 대기
		shutdownExecutorService(executorService);
		long endTime = System.currentTimeMillis();
		log.info("전체 테스트 소요 시간: {} ms", (endTime - startTime));

		PerformanceMetrics metrics = performanceTracker.generatePerformanceMetrics();
		performanceTracker.logPerformanceMetrics(metrics);

		return ResponseEntity.ok().build();

	}

	@GetMapping("/cache")
	public ResponseEntity<Map<String, MarketCondition>> getMarketConditions() {
		return ResponseEntity.ok(marketStateCache.getAllConditions());
	}

	@GetMapping("/cache/{companyCode}")
	public ResponseEntity<MarketCondition> getMarketCondition(@PathVariable String companyCode) {
		MarketCondition condition = marketStateCache.getCondition(companyCode);
		if (condition == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(condition);
	}

	/**
	 * 개별 주문 처리 메서드
	 * @param order 처리할 주문
	 */
	private void processOrder(Order order) {
		try {
			// 주문 생성 추적
			performanceTracker.trackOrderCreation(order);
			performanceTracker.trackOrderProcessingStart(order.getId().toString());

			// 주문 매칭 처리
			boolean matchResult = orderService.ProcessOrder(order);
			String orderId = order.getId().toString();

			if (!performanceTracker.isOrderTracked(orderId)) {
				performanceTracker.trackOrderCreation(order);
			}
			performanceTracker.trackOrderProcessingStart(orderId);
			performanceTracker.trackOrderMatched(orderId, matchResult);

		} catch (Exception e) {
			// 예외 처리
			log.error("Order processing error", e);
			performanceTracker.trackOrderMatched(
				order.getId().toString(),
				false
			);
		}
	}

	/**
	 * 스레드 풀 종료 메서드
	 * @param executorService 종료할 스레드 풀
	 */
	private void shutdownExecutorService(ExecutorService executorService) {
		executorService.shutdown();
		try {
			// 모든 작업 완료 대기 (최대 5분)
			if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			log.error("Execution interrupted", e);
		}
	}

	/**
	 * 테스트 시나리오 생성 엔드포인트
	 * @return 다양한 테스트 시나리오 목록
	 */
	@GetMapping("/test-scenarios")
	public ResponseEntity<List<TestScenario>> generateTestScenarios() {
		List<TestScenario> scenarios = List.of(
			createTestScenario(1, "Low Load Test", 100, 5),
			createTestScenario(2, "Medium Load Test", 1000, 20),
			createTestScenario(3, "High Load Test", 5000, 50),
			createTestScenario(4, "Stress Test", 10000, 100)
		);
		return ResponseEntity.ok(scenarios);
	}

	/**
	 * 테스트 시나리오 생성 헬퍼 메서드
	 */
	private TestScenario createTestScenario(int id, String description,
		int orderCount, int concurrentThreads) {
		return TestScenario.builder()
			.scenarioId(id)
			.description(description)
			.orderCount(orderCount)
			.concurrentThreads(concurrentThreads)
			.build();
	}
}
