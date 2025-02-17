// package org.scoula.backend.order.service.websocket;
//
// import java.net.URI;
// import java.net.URISyntaxException;
//
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.client.WebSocketClient;
//
// import jakarta.annotation.PostConstruct;
// import lombok.extern.slf4j.Slf4j;
//
// @Component
// @Slf4j
// public class WebSocketConnection {
// 	private final WebSocketClient client;
// 	private final CustomWebSocketHandler handler;
//
// 	@Value("${websocket.server.url}")
// 	private String wsUrl;
//
// 	public WebSocketConnection(WebSocketClient client, CustomWebSocketHandler handler) {
// 		this.client = client;
// 		this.handler = handler;
// 	}
//
// 	//@postconstruct
// 	//의존성 주입 후 초기화 ->
// 	@PostConstruct
// 	public void connect() {
// 		try {
// 			URI uri = new URI(wsUrl);
// 			client.execute(handler, uri.toString());
// 			// handler.afterConnectionEstablished(null);
// 			log.info("Websocket 접속: {}", wsUrl);
// 		} catch (URISyntaxException e) {
// 			log.error("WebSocket 연결 실패: 잘못된 URI", e);
// 		} catch (Exception e) {
// 			log.error("WebSocket 연결 실패", e);
// 		}
// 	}
// }
//
