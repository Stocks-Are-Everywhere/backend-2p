package org.scoula.backend.global.config;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.socket.client.WebSocketClient;
// import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Configuration
// public class WebSocketConfig {
//
// 	@Bean
// 	public WebSocketClient webSocketClient() {
// 		return new StandardWebSocketClient();
// 	}
// }

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 구독 주제(subscribe)는 "/topic"으로 시작
		config.enableSimpleBroker("/topic");
		// 클라이언트에서 보내는 메시지 주제는 "/app"으로 시작
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")  // WebSocket 엔드포인트 설정
				.setAllowedOrigins("http://localhost:3000")  // React 앱의 주소
				.withSockJS();  // SockJS 지원 추가
	}
}
