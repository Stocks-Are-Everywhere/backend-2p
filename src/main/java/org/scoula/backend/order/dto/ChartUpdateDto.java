package org.scoula.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChartUpdateDto {
	private double price;
	private int volume;
}
