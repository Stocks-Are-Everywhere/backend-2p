package org.scoula.backend.order.service.multiqueue.monitoring;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestMatrix {
	// 테스트 시나리오 관련 메트릭스
	private List<TestScenario> scenarios;

	// 전체 성능 요약
	private PerformanceMetrics overallPerformance;

	// 시나리오별 성능 결과
	private Map<Integer, PerformanceMetrics> scenarioPerformanceResults;

	// 성능 통계
	@Data
	@Builder
	public static class PerformanceStatistics {
		private double averageProcessingTime;
		private double throughput;
		private double errorRate;
	}
}
