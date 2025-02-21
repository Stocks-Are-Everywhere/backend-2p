package org.scoula.backend.order.service.kiswebsocket;

import lombok.Data;

@Data
public class StockDataResponse {
	private String code;
	private StockData data;
}
