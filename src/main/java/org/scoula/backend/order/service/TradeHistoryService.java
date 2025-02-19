package org.scoula.backend.order.service;

import java.util.List;
import java.util.stream.Collectors;

import org.scoula.backend.order.controller.response.TradeHistoryResponse;
import org.scoula.backend.order.domain.TradeHistory;
import org.scoula.backend.order.repository.TradeHistoryRepositoryImpl;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {
	private final TradeHistoryRepositoryImpl tradeHistoryRepository;

	public void saveTradeHistory(TradeHistoryResponse tradeHistoryResponse) {
		TradeHistory tradeHistory = convertToEntity(tradeHistoryResponse);
		tradeHistoryRepository.save(tradeHistory);
	}

	public List<TradeHistoryResponse> getTradeHistory() {
		List<TradeHistory> tradeHistories = tradeHistoryRepository.getTradeHistory();
		return tradeHistories.stream()
			.map(this::convertToDto)
			.collect(Collectors.toList());
	}

	private TradeHistory convertToEntity(TradeHistoryResponse dto) {
		return TradeHistory.builder()
			.id(dto.id())
			.sellOrderId(dto.sellOrderId())
			.buyOrderId(dto.buyOrderId())
			.quantity(dto.quantitiy())  // Note: There's a typo in the DTO field name
			.price(dto.price())
			.build();
	}

	private TradeHistoryResponse convertToDto(TradeHistory entity) {
		return TradeHistoryResponse.builder()
			.id(entity.getId())
			.sellOrderId(entity.getSellOrderId())
			.buyOrderId(entity.getBuyOrderId())
			.quantitiy(entity.getQuantity())  // Note: Keeping the typo to match the DTO
			.price(entity.getPrice())
			.build();
	}
}

