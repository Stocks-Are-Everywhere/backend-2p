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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
		log.info("Received message: {}", payload);

		// 상세 로깅
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(payload);
			log.debug("Parsed message: {}", jsonNode.toPrettyString());
		} catch (JsonProcessingException e) {
			log.error("Failed to parse message", e);
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

