package org.scoula.backend.order.controller.response;

import java.util.List;

import org.scoula.backend.order.dto.PriceLevelDto;

import lombok.Builder;

@Builder
public record OrderBookResponse(
		String companyCode,
		List<PriceLevelDto> askLevels,
		List<PriceLevelDto> bidLevels
) {
}
