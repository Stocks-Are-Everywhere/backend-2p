package org.scoula.backend.order.service;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final MockDataGenerator mockDataGenerator;

	@Override
	public Order createOrder() {
		return mockDataGenerator.createMockOrder();
	}

	@Override
	public boolean validateOrder(Order order) {
		// 기본 필드 검증
		if (order == null ||
			order.getCompanyCode() == null ||
			order.getType() == null ||
			order.getStatus() == null ||
			order.getAccount() == null) {
			return false;
		}

		// 수량 검증
		if (order.getTotalQuantity() <= 0 ||
			order.getRemainingQuantity() < 0 ||
			order.getRemainingQuantity() > order.getTotalQuantity()) {
			return false;
		}

		// 가격 검증
		if (order.getPrice() <= 0) {
			return false;
		}

		// 계좌 잔고 검증 (매수 주문의 경우)
		if (order.getType() == Type.BUY) {
			long totalOrderAmount = (long)order.getPrice() * order.getTotalQuantity();
			if (order.getAccount().getBalance() < totalOrderAmount) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Order createExecuteCondition(Order order) {
		return null;
	}

	@Override
	public boolean validateExecuteCondition(Order order) {
		return false;
	}
}
