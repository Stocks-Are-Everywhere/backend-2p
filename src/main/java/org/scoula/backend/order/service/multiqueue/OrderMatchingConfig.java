package org.scoula.backend.order.service.multiqueue;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class OrderMatchingConfig {

	@Bean
	public Executor orderMatchingExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 기본 스레드 개수
		executor.setCorePoolSize(4);
		// 최대 스레드 개수
		executor.setMaxPoolSize(8);
		// 작업 대기열 크기
		executor.setQueueCapacity(500);
		// 스레드 식별자
		executor.setThreadNamePrefix("OrderMatching-");
		executor.initialize();
		return executor;
	}

}

