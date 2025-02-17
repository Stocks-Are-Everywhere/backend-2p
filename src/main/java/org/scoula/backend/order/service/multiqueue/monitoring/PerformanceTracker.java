package org.scoula.backend.order.service.multiqueue.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.scoula.backend.order.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PerformanceTracker {

	private final SystemMetricsCollector systemMetricsCollector;

	@Autowired
	public PerformanceTracker(SystemMetricsCollector systemMetricsCollector) {
		this.systemMetricsCollector = systemMetricsCollector;
	}

	private final ConcurrentMap<String, OrderPerformanceData> orderTrackingMap =
		new ConcurrentHashMap<>();

	// 내부 성능 데이터 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class OrderPerformanceData {
		long orderCreationTime;
		long orderProcessingStartTime;
		long orderProcessingEndTime;
		String orderId;
		String companyCode;
		OrderStatus status;

		enum OrderStatus {
			CREATED, PROCESSING, MATCHED, FAILED
		}
	}

	// 주문 생성 추적
	public void trackOrderCreation(Order order) {
		OrderPerformanceData data = OrderPerformanceData.builder()
			.orderCreationTime(System.nanoTime())
			.orderId(order.getId().toString())
			.companyCode(order.getCompanyCode())
			.status(OrderPerformanceData.OrderStatus.CREATED)
			.build();

		orderTrackingMap.put(data.getOrderId(), data);
	}

	// 주문 처리 시작 추적
	public void trackOrderProcessingStart(String orderId) {
		OrderPerformanceData data = orderTrackingMap.get(orderId);
		if (data != null) {
			data.setOrderProcessingStartTime(System.nanoTime());
			data.setStatus(OrderPerformanceData.OrderStatus.PROCESSING);
		}
	}

	// 주문 매칭 완료 추적
	public void trackOrderMatched(String orderId, boolean success) {
		OrderPerformanceData data = orderTrackingMap.get(orderId);
		if (data != null) {
			data.setOrderProcessingEndTime(System.nanoTime());
			data.setStatus(success ?
				OrderPerformanceData.OrderStatus.MATCHED :
				OrderPerformanceData.OrderStatus.FAILED);
		}
	}

	public boolean isOrderTracked(String orderId) {
		return orderTrackingMap.containsKey(orderId);
	}

	public PerformanceMetrics generatePerformanceMetrics() {
		List<OrderPerformanceData> completedOrders = orderTrackingMap.values().stream()
			.filter(data -> data.getStatus() == OrderPerformanceData.OrderStatus.MATCHED
				|| data.getStatus() == OrderPerformanceData.OrderStatus.FAILED)
			.collect(Collectors.toList());

		// 성능 요약 생성
		PerformanceMetrics.PerformanceSummary performanceSummary = createPerformanceSummary(completedOrders);

		// 주문 통계 생성
		PerformanceMetrics.OrderStatistics orderStatistics = createOrderStatistics(completedOrders);

		return PerformanceMetrics.builder()
			.totalOrders(completedOrders.size())
			.totalProcessingTime(calculateTotalProcessingTime(completedOrders))
			.averageProcessingTimePerOrder(calculateAverageProcessingTime(completedOrders))
			.performanceSummary(performanceSummary)
			.orderStatistics(orderStatistics)
			.matchingStatistics(createMatchingStats(completedOrders))
			.concurrencyStatistics(createConcurrencyStats(completedOrders))
			.errorStatistics(createErrorStats(completedOrders))
			.build();
	}

	// 성능 요약 생성 메서드 추가
	private PerformanceMetrics.PerformanceSummary createPerformanceSummary(
		List<OrderPerformanceData> completedOrders
	) {
		long totalProcessingTime = calculateTotalProcessingTime(completedOrders);
		double throughput = totalProcessingTime > 0 ?
			(completedOrders.size() * 1000.0) / totalProcessingTime : 0.0;

		return PerformanceMetrics.PerformanceSummary.builder()
			.throughput(throughput)
			.responseTime(calculateAverageProcessingTime(completedOrders))
			.build();
	}

	// 주문 통계 생성 메서드 추가
	private PerformanceMetrics.OrderStatistics createOrderStatistics(
		List<OrderPerformanceData> completedOrders
	) {
		// 회사별 주문 수 계산
		Map<String, Integer> ordersByCompany = completedOrders.stream()
			.collect(Collectors.groupingBy(
				OrderPerformanceData::getCompanyCode,
				Collectors.summingInt(e -> 1)
			));

		// 회사별 평균 처리 시간 계산 (예시)
		Map<String, Double> orderProcessingTimeByCompany = completedOrders.stream()
			.collect(Collectors.groupingBy(
				OrderPerformanceData::getCompanyCode,
				Collectors.averagingLong(data ->
					(data.getOrderProcessingEndTime() - data.getOrderProcessingStartTime()) / 1_000_000
				)
			));

		return PerformanceMetrics.OrderStatistics.builder()
			.totalOrdersProcessed(completedOrders.size())
			.buyOrders(0)  // 실제 buy/sell 구분 로직 필요
			.sellOrders(0)
			.ordersByCompany(ordersByCompany)
			.orderProcessingTimeByCompany(orderProcessingTimeByCompany)
			.build();
	}

	// 총 처리 시간 계산 메서드 추가
	private long calculateTotalProcessingTime(List<OrderPerformanceData> completedOrders) {
		return completedOrders.stream()
			.mapToLong(data -> {
				if (data.getOrderProcessingEndTime() > 0 && data.getOrderProcessingStartTime() > 0) {
					long processingTime = data.getOrderProcessingEndTime() - data.getOrderProcessingStartTime();
					return Math.max(0, processingTime / 1_000_000); // 나노초를 밀리초로 변환
				}
				return 0;
			})
			.sum();
	}

	// 매칭 통계 생성
	private PerformanceMetrics.MatchingStatistics createMatchingStats(
		List<OrderPerformanceData> completedOrders
	) {
		Map<String, Integer> matchesByCompany = completedOrders.stream()
			.filter(data -> data.getStatus() == OrderPerformanceData.OrderStatus.MATCHED)
			.collect(Collectors.groupingBy(
				OrderPerformanceData::getCompanyCode,
				Collectors.summingInt(e -> 1)
			));

		int totalMatches = matchesByCompany.values().stream().mapToInt(Integer::intValue).sum();
		int successfulMatches = totalMatches;
		int failedMatches = completedOrders.size() - totalMatches;

		return PerformanceMetrics.MatchingStatistics.builder()
			.totalMatches(completedOrders.size())
			.successfulMatches(successfulMatches)
			.failedMatches(failedMatches)
			.matchesByCompany(matchesByCompany)
			.matchSuccessRate(calculateMatchSuccessRate(totalMatches, completedOrders.size()))
			.build();
	}

	// 동시성 통계 생성
	private PerformanceMetrics.ConcurrencyStatistics createConcurrencyStats(
		List<OrderPerformanceData> completedOrders
	) {
		return PerformanceMetrics.ConcurrencyStatistics.builder()
			.maxConcurrentThreads(Thread.activeCount())
			.totalThreadsCreated(completedOrders.size())
			.build();
	}

	// 에러 통계 생성
	private PerformanceMetrics.ErrorStatistics createErrorStats(
		List<OrderPerformanceData> completedOrders
	) {
		Map<String, Integer> errorsByType = completedOrders.stream()
			.filter(data -> data.getStatus() == OrderPerformanceData.OrderStatus.FAILED)
			.collect(Collectors.groupingBy(
				data -> data.getStatus().name(),
				Collectors.summingInt(e -> 1)
			));

		return PerformanceMetrics.ErrorStatistics.builder()
			.totalErrors(errorsByType.values().stream().mapToInt(Integer::intValue).sum())
			.errorsByType(errorsByType)
			.build();
	}

	// 평균 처리 시간 계산
	private double calculateAverageProcessingTime(List<OrderPerformanceData> completedOrders) {
		if (completedOrders.isEmpty()) {
			return 0.0;
		}

		return completedOrders.stream()
			.mapToLong(data -> {
				if (data.getOrderProcessingEndTime() > 0 && data.getOrderProcessingStartTime() > 0) {
					return (data.getOrderProcessingEndTime() - data.getOrderProcessingStartTime());
				}
				return 0;
			})
			.filter(time -> time > 0)
			.average()
			.orElse(0.0) / 1_000_000.0; // 스트림 연산 후 나노초를 밀리초로 변환
	}

	// 매칭 성공률 계산
	private double calculateMatchSuccessRate(int successfulMatches, int totalOrders) {
		return totalOrders > 0
			? (double)successfulMatches / totalOrders
			: 0.0;
	}

	public void logPerformanceMetrics(PerformanceMetrics metrics) {

		SystemMetrics systemMetrics = systemMetricsCollector.collectCurrentMetrics();

		log.info("테스트 레벨");
		log.info("synchronized 적용 단계");

		log.info("==========================");
		log.info("concurrent 적용 단계");

		log.info("============================");
		log.info("==== 성능 테스트 결과 ====");
		log.info("테스트 환경:");
		log.info("- 총 주문 수: {}", metrics.getTotalOrders());
		log.info("- 동시 처리 스레드 수: {}",
			metrics.getConcurrencyStatistics().getMaxConcurrentThreads());
		log.info("처리 시간:");
		// -> performance metrics 에서 getter말고 메서드화하여 실제 측정되게 만들어야한다?
		// 수정 필요
		log.info("- 총 처리 시간: {} ms", metrics.getTotalProcessingTime());
		log.info("- 주문당 평균 처리 시간: {} ms",
			String.format("%.2f", metrics.getAverageProcessingTimePerOrder()));
		log.info("- 처리량(초당 주문 수): {}",
			String.format("%.2f", metrics.getPerformanceSummary().getThroughput()));
		log.info("\n시스템 리소스 사용:");
		log.info("- CPU 사용률: {}%", String.format("%.2f", systemMetrics.getCpuUsage() * 100));
		log.info("- 메모리 사용량: {} MB", String.format("%.2f", systemMetrics.getMemoryUsed() / (1024 * 1024)));
		log.info("- 활성 스레드 수: {}", systemMetrics.getThreadCount());

		log.info("\n처리 결과:");
		log.info("- 성공적으로 체결된 주문 수: {}", metrics.getMatchingStatistics().getSuccessfulMatches());
		log.info("- 실패한 주문 수: {}", metrics.getMatchingStatistics().getFailedMatches());
		log.info("- 매칭 성공률: {}%",
			String.format("%.2f", metrics.getMatchingStatistics().getMatchSuccessRate() * 100));
		log.info("- 오류율: {}%",
			String.format("%.2f", metrics.getErrorStatistics().getErrorRate() * 100));

		log.info("\n회사별 처리 통계:");
		metrics.getOrderStatistics().getOrdersByCompany().forEach((company, count) ->
			log.info("- {}: {} 주문", company, count));

		log.info("=====================");
	}

}
