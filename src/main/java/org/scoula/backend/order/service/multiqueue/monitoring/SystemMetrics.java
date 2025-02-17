package org.scoula.backend.order.service.multiqueue.monitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {
	private double cpuUsage;      // CPU 사용률 (0.0 ~ 1.0)
	private double memoryUsed;    // 사용 중인 메모리 (바이트)
	private long threadCount;     // 활성 스레드 수

	// 추가 메트릭이 필요한 경우 여기에 필드 추가
	private double heapMemoryUsed;        // 힙 메모리 사용량
	private double nonHeapMemoryUsed;     // 비힙 메모리 사용량
	private long totalMemory;             // 전체 메모리
	private int processorCount;           // 프로세서 수
}

