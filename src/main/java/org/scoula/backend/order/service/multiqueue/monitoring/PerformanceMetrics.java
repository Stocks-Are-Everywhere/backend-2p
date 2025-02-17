package org.scoula.backend.order.service.multiqueue.monitoring;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PerformanceMetrics {
	// 기본 성능 지표
	private long totalProcessingTime;      // 총 처리 시간 (밀리초)
	private int totalOrders;                // 총 주문 수
	private double averageProcessingTimePerOrder; // 주문당 평균 처리 시간

	// 성능 요약 지표
	private PerformanceSummary performanceSummary;

	// 주문 처리 통계
	private OrderStatistics orderStatistics;

	// 매칭 통계
	private MatchingStatistics matchingStatistics;

	// 스레드 및 동시성 통계
	private ConcurrencyStatistics concurrencyStatistics;

	// 오류 통계
	private ErrorStatistics errorStatistics;

	// 성능 요약 내부 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PerformanceSummary {
		private double throughput;          // 초당 처리 주문 수
		private double responseTime;        // 평균 응답 시간
		private double cpuUtilization;      // CPU 사용률
		private double memoryUtilization;   // 메모리 사용률
	}

	// 주문 통계 내부 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderStatistics {
		private int totalOrdersProcessed;
		private int buyOrders;
		private int sellOrders;
		private Map<String, Integer> ordersByCompany;
		private Map<String, Double> orderProcessingTimeByCompany;
	}

	// 매칭 통계 내부 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MatchingStatistics {
		private int totalMatches;
		private int successfulMatches;
		private int failedMatches;
		private double matchSuccessRate;
		private Map<String, Integer> matchesByCompany;
	}

	// 동시성 통계 내부 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ConcurrencyStatistics {
		private int maxConcurrentThreads;
		private int totalThreadsCreated;
		private long averageThreadLifetime;
		private double threadUtilization;
	}

	// 오류 통계 내부 클래스
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ErrorStatistics {
		private int totalErrors;
		private Map<String, Integer> errorsByType;
		private List<String> detailedErrorLogs;
		private double errorRate;
	}

	// 빌더 메서드 추가
	public static PerformanceMetricsBuilder builder() {
		return new PerformanceMetricsBuilder();
	}

	/**
	 * 성능 지표 비교 메서드
	 * @param other 비교할 다른 성능 메트릭스
	 * @return 성능 비교 결과 문자열
	 */
	public String comparePerformance(PerformanceMetrics other) {
		StringBuilder comparison = new StringBuilder("Performance Comparison:\n");

		comparison.append(String.format("Total Orders: %d vs %d\n",
			this.totalOrders, other.totalOrders));

		comparison.append(String.format("Total Processing Time: %d ms vs %d ms\n",
			this.totalProcessingTime, other.totalProcessingTime));

		comparison.append(String.format("Avg Processing Time per Order: %.2f ms vs %.2f ms\n",
			this.averageProcessingTimePerOrder,
			other.averageProcessingTimePerOrder));

		comparison.append(String.format("Throughput: %.2f orders/sec vs %.2f orders/sec\n",
			this.performanceSummary.getThroughput(),
			other.performanceSummary.getThroughput()));

		comparison.append(String.format("Match Success Rate: %.2f%% vs %.2f%%\n",
			this.matchingStatistics.getMatchSuccessRate() * 100,
			other.matchingStatistics.getMatchSuccessRate() * 100));

		return comparison.toString();
	}

	/**
	 * JSON 형식으로 성능 메트릭스 직렬화
	 * @return JSON 문자열
	 */
	public String toJson() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error("JSON 변환 중 오류 발생", e);
			return "{}";
		}
	}

	/**
	 * CSV 형식으로 성능 메트릭스 내보내기
	 * @return CSV 문자열
	 */
	public String toCSV() {
		return String.format(
			"Metric,Value\n" +
				"Total Orders,%d\n" +
				"Total Processing Time,%d\n" +
				"Avg Processing Time per Order,%.2f\n" +
				"Throughput,%.2f\n" +
				"Match Success Rate,%.2f\n" +
				"Error Rate,%.2f",
			totalOrders,
			totalProcessingTime,
			averageProcessingTimePerOrder,
			performanceSummary.getThroughput(),
			matchingStatistics.getMatchSuccessRate(),
			errorStatistics.getErrorRate()
		);
	}

	/**
	 * 성능 임계값 초과 여부 확인
	 * @param thresholds 성능 임계값 객체
	 * @return 임계값 초과 여부
	 */
	public boolean exceedsThresholds(PerformanceThresholds thresholds) {
		return this.averageProcessingTimePerOrder > thresholds.getMaxAvgProcessingTime() ||
			this.matchingStatistics.getMatchSuccessRate() < thresholds.getMinMatchSuccessRate() ||
			this.errorStatistics.getErrorRate() > thresholds.getMaxErrorRate();
	}

	// 임계값 설정을 위한 내부 클래스
	@Data
	@Builder
	public static class PerformanceThresholds {
		private double maxAvgProcessingTime;    // 최대 평균 처리 시간
		private double minMatchSuccessRate;     // 최소 매칭 성공률
		private double maxErrorRate;            // 최대 오류율
	}

	// 문자열 요약 메서드
	public String generateSummaryReport() {
		return String.format(
			"Performance Metrics Summary:\n" +
				"Total Orders: %d\n" +
				"Total Processing Time: %d ms\n" +
				"Avg Processing Time per Order: %.2f ms\n" +
				"Throughput: %.2f orders/sec\n" +
				"Match Success Rate: %.2f%%\n" +
				"Error Rate: %.2f%%",
			totalOrders,
			totalProcessingTime,
			averageProcessingTimePerOrder,
			performanceSummary.getThroughput(),
			matchingStatistics.getMatchSuccessRate() * 100,
			errorStatistics.getErrorRate() * 100
		);
	}
}
