package org.scoula.backend.order.controller.request;

import org.scoula.backend.order.domain.OrderStatus;
import org.scoula.backend.order.domain.Type;

public record OrderRequest(
		String companyCode,
		Type type,
		Integer totalQuantity,
		Integer remainingQuantity,
		OrderStatus status,
		Integer price,
		Long accountId
) {
}
