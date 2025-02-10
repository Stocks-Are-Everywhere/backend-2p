package org.scoula.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebsocketConfig {

	@Bean
	public WebSocketClient webSocketClient() {
		return new StandardWebSocketClient();
	}
}
