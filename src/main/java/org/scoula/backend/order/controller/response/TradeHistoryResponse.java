package org.scoula.backend.order.controller.response;

import lombok.Builder;

@Builder
public record TradeHistoryResponse(
	Long id,
	Long sellOrderId,
	Long buyOrderId,
	Integer quantitiy,
	Integer price
) {
}
