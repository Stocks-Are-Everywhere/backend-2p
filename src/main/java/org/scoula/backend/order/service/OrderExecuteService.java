package org.scoula.backend.order.service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.scoula.backend.order.domain.Order;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExecuteService {
	//100 Average time per order: 0.044264 ms
	//1000 Average time per order: 0.030501 ms
	//10000 Average time per order: 0.009164 ms
	//100000 Average time per order: 0.001368 ms, Total time: 125.6617 ms
	//1000000 Average time per order: 0.001606 ms, Total time: 1509.5105 ms
	//10000000 Average time per order: 0.00000071초, Total time: 7011.6214 ms
	private final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();

	//100 Average time per order: 0.02716 ms
	//1000 Average time per order: 0.019274 ms
	//10000 Average time per order: 0.000559 ms
	//100000 Average time per order: 0.001074 ms, Total time: 101.6594 ms
	//1000000 Average time per order: 0.001523 ms, Total time: 1392.0076 ms
	//10000000 Average time per order: 0.001067 ms, Total time: 10177.8496 ms
	// private final Queue<Order> orderQueue = new ArrayDeque<>();

	//100 Average time per order: 0.2656 ms
	//1000 Average time per order: 0.011937 ms
	//10000 Average time per order: 0.002483 ms
	//100000 Average time per order: 0.009062 ms, Total time: 524.376 ms
	//1000000 Average time per order: 0.012208 ms, Total time: 6669.1569 ms
	//10000000 Average time per order: 0.01222 ms, Total time: 64836.3791 ms
	// private final Queue<Order> orderQueue = new LinkedList<>();
	private final MockDataGenerator mockDataGenerator;
	private final OrderService orderService;

	@PostConstruct
	public void processOrderQueue() {
		// 테스트할 주문 수량 배열
		int[] orderCounts = {10000000};

		for (int count : orderCounts) {
			log.info("Starting test with {} orders", count);
			runTestWithOrders(count);
			log.info("Completed test with {} orders\n", count);
		}
	}

	private void runTestWithOrders(int count) {
		orderQueue.clear();
		long startTime = System.nanoTime();
		int processedCount = 0;

		// MockDataGenerator를 사용하여 여러 주문 생성
		List<Order> orders = mockDataGenerator.createMockOrders(count);
		orders.forEach(orderQueue::offer);

		log.info("Generated {} orders in {} ms",
			count,
			(System.nanoTime() - startTime) / 1_000_000.0);

		// 큐 처리
		while (!orderQueue.isEmpty()) {
			Order queuedOrder = orderQueue.peek();
			long orderStartTime = System.nanoTime();

			if (orderService.validateOrder(queuedOrder)) {
				orderQueue.poll();
				processedCount++;
				log.debug("Order processed - ID: {}, Stock: {}, Quantity: {}, Processing Time: {} ms",
					queuedOrder.getId(),
					queuedOrder.getCompanyCode(),
					queuedOrder.getTotalQuantity(),
					(System.nanoTime() - orderStartTime) / 1_000_000.0);
			} else {
				orderQueue.poll();
				log.warn("Order validation failed - ID: {}", queuedOrder.getId());
			}
		}

		long totalTime = System.nanoTime() - startTime;
		log.info("Test Summary for {} orders:", count);
		log.info("- Total processed: {}", processedCount);
		log.info("- Total time: {} ms", totalTime / 1_000_000.0);
		log.info("- Average time per order: {} ms", (totalTime / processedCount) / 1_000_000.0);
	}
}
