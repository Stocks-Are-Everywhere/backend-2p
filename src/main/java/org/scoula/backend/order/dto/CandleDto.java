package org.scoula.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CandleDto {
	private long time;       // Unix timestamp (초 단위)
	private double open;     // 시가
	private double high;     // 고가
	private double low;      // 저가
	private double close;    // 종가
	private int volume;      // 거래량
}
