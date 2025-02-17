package org.scoula.backend.order.service.multiqueue;

import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.Type;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderRequest {
	private String companyCode;
	private Type type;
	private Integer price;
	private Integer quantity;
	private Long accountId;  // Postman 테스트를 위해 accountId만 받음

	// Order 엔티티로 변환하는 메서드
	public static OrderRequest from(Order order) {
		return OrderRequest.builder()
			.companyCode(order.getCompanyCode())
			.type(order.getType())
			.price(order.getPrice())
			.quantity(order.getTotalQuantity())
			.accountId(order.getAccount() != null ? order.getAccount().getId() : null)
			.build();
	}

	// 더미 주문 생성
	public static OrderRequest createDummy(String companyCode, Type type, int price) {
		return OrderRequest.builder()
			.companyCode(companyCode)
			.type(type)
			.price(100000)
			.quantity(100)  // 더미 수량
			.accountId(0L)  // 더미 계정 ID
			.build();
	}

}

