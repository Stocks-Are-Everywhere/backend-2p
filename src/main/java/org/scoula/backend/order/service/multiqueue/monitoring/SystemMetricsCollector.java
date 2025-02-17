package org.scoula.backend.order.service.multiqueue.monitoring;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SystemMetricsCollector {
	private final MeterRegistry meterRegistry;

	public SystemMetricsCollector(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public SystemMetrics collectCurrentMetrics() {
		double cpuUsage = meterRegistry.get("process.cpu.usage")
			.gauge().value();
		double memoryUsed = meterRegistry.get("jvm.memory.used")
			.gauge().value();
		long threadCount = (long)meterRegistry.get("jvm.threads.live")
			.gauge().value();

		return SystemMetrics.builder()
			.cpuUsage(cpuUsage)
			.memoryUsed(memoryUsed)
			.threadCount(threadCount)
			.build();
	}
}

