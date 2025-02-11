package org.scoula.backend.order.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomWebSocketHandler extends TextWebSocketHandler {
	private WebSocketSession session;

	@Value("${websocket.approval.key}")
	private String approvalKey;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		this.session = session;
		log.info("WebSocket Connected");
		sendAuthenticationMessage();
	}

	private void sendAuthenticationMessage() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> message = new HashMap<>();

			// Header
			Map<String, String> header = new HashMap<>();
			header.put("approval_key", approvalKey);
			header.put("custtype", "P");
			header.put("tr_type", "1");
			header.put("content-type", "utf-8");

			// Body
			Map<String, Object> body = new HashMap<>();
			Map<String, String> input = new HashMap<>();
			input.put("tr_id", "H0STCNT0");
			input.put("tr_key", "005930");
			body.put("input", input);

			// 전체 메시지
			message.put("header", header);
			message.put("body", body);

			String jsonMessage = objectMapper.writeValueAsString(message);
			session.sendMessage(new TextMessage(jsonMessage));
			log.info("Authentication message sent: {}", jsonMessage);

		} catch (IOException e) {
			log.error("Failed to send authentication message", e);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		String payload = message.getPayload();
		log.info("수신된 메시지: {}", payload);

		// 파이프(|)로 메시지 분리
		String[] fields = payload.split("\\|");

		// '^'로 구분된 나머지 데이터 파싱
		if (fields.length >= 3) {
			String messageType = fields[0];    // 메시지 타입
			String trId = fields[1];           // 거래 ID
			String count = fields[2];          // 카운트

			if (fields.length > 3) {
				String[] stockData = fields[3].split("\\^");
				processStockData(stockData);
			}
		}
	}

	private void processStockData(String[] stockData) {
		if (stockData.length > 0) {
			try {
				// 주식 데이터 필드 파싱 예시
				Map<String, String> parsedData = new HashMap<>();
				parsedData.put("종목코드", stockData[0]);
				parsedData.put("시간", stockData[1]);
				parsedData.put("현재가", stockData[2]);
				// 필요한 필드 추가

				log.debug("파싱된 주식 데이터: {}", parsedData);
			} catch (Exception e) {
				log.error("주식 데이터 처리 중 오류 발생", e);
			}
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		log.error("Transport error: ", exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info("WebSocket connection closed: {}", status);
		this.session = null;
	}

	public boolean isConnected() {
		return session != null && session.isOpen();
	}

	public void sendMessage(String message) {
		try {
			if (isConnected()) {
				session.sendMessage(new TextMessage(message));
			}
		} catch (IOException e) {
			log.error("Failed to send message", e);
		}
	}
}

