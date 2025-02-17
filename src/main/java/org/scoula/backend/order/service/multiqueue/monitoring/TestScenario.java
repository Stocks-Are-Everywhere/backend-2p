package org.scoula.backend.order.service.multiqueue.monitoring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestScenario {
	private int scenarioId;
	private String description;
	private int orderCount;
	private int concurrentThreads;
}
