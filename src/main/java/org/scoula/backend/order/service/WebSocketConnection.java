package org.scoula.backend.order.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketConnection {
	private final WebSocketClient client;
	private final CustomWebSocketHandler handler;

	@Value("${websocket.server.url}")
	private String wsUrl;

	public WebSocketConnection(WebSocketClient client, CustomWebSocketHandler handler) {
		this.client = client;
		this.handler = handler;
	}

	@PostConstruct
	public void connect() {
		try {
			client.execute(handler, String.valueOf(new URI(wsUrl)));
			log.info("Websocket 접속: {}", wsUrl);
		} catch (URISyntaxException e) {
			log.error("WebSocket 연결 실패", e);
		}
	}
}

