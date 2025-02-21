package org.scoula.backend.order.service.kiswebsocket;

import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.json.simple.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KisWebSocketClient {
	private WebSocketClient client;
	private final String KIS_WS_URL = "ws://ops.koreainvestment.com:31000/tryitout/H0STCNT0";
	// private final String KIS_WS_URL = "ws://localhost:31000";
	private final String APPROVAL_KEY = "";
	private WebSocketSession session;
	private final SimpMessagingTemplate messagingTemplate;

	public void connect(String stockCode) {
		client = new StandardWebSocketClient();

		WebSocketHandler handler = new WebSocketHandler() {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				log.info("Connected to KIS WebSocket server");
				KisWebSocketClient.this.session = session;
				sendSubscribeMessage(session, stockCode);
			}

			@Override
			public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
				try {
					String payload = (String)message.getPayload();
					log.info(payload);

					// 연결 확인 응답 메시지인지 확인
					if (payload.startsWith("{")) {
						// JSON 응답 처리 (연결 확인 등)
						log.info("Received connection response: {}", payload);
						return;
					}

					// 실제 주식 데이터 처리
					StockData stockData = parseKisData(payload);
					messagingTemplate.convertAndSend("/topic/stockdata/" + stockCode, stockData);
				} catch (Exception e) {
					log.error("Error handling message: {}", e.getMessage());
				}
			}

			@Override
			public void handleTransportError(WebSocketSession session, Throwable exception) {
				log.error("Transport error: ", exception);
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
				log.info("Connection closed: {}", closeStatus);
			}

			@Override
			public boolean supportsPartialMessages() {
				return false;
			}
		};

		try {
			client.execute(handler, new WebSocketHttpHeaders(), URI.create(KIS_WS_URL));
		} catch (Exception e) {
			log.error("Failed to connect to KIS WebSocket server", e);
			throw new RuntimeException("WebSocket connection failed", e);
		}
	}

	private void sendSubscribeMessage(WebSocketSession session, String stockCode) throws IOException {
		JSONObject request = createSubscribeRequest(stockCode);
		session.sendMessage(new TextMessage(request.toString()));
	}

	private JSONObject createSubscribeRequest(String stockCode) {
		JSONObject header = new JSONObject();
		header.put("approval_key", APPROVAL_KEY);
		header.put("custtype", "P");
		header.put("tr_type", "1");
		header.put("content-type", "utf-8");

		JSONObject input = new JSONObject();
		input.put("tr_id", "H0STCNT0");
		input.put("tr_key", stockCode);

		JSONObject body = new JSONObject();
		body.put("input", input);

		JSONObject request = new JSONObject();
		request.put("header", header);
		request.put("body", body);

		return request;
	}

	public void disconnect() {
		if (session != null && session.isOpen()) {
			try {
				session.close();
				log.info("WebSocket connection closed");
			} catch (IOException e) {
				log.error("Error closing WebSocket connection", e);
			}
		}
	}

	private StockData parseKisData(String rawData) {
		try {
			String[] sections = rawData.split("\\|");
			if (sections.length < 4) {
				log.error("잘못된 데이터 형식: {}", rawData);
				throw new IllegalArgumentException("잘못된 데이터 형식");
			}

			String[] fields = sections[3].split("\\^");
			StockData data = new StockData();

			try {
				// 기본 정보 설정
				data.setTime(fields[1]);

				// 숫자 데이터 파싱 시 DecimalFormat 사용
				DecimalFormat df = new DecimalFormat("#.##");
				df.setParseBigDecimal(true);

				// 가격 정보 설정
				data.setCurrentPrice(df.parse(fields[2]).doubleValue());
				data.setChangePrice(df.parse(fields[4]).doubleValue());
				data.setChangeRate(df.parse(fields[5]).doubleValue());
				data.setOpenPrice(df.parse(fields[7]).doubleValue());
				data.setHighPrice(df.parse(fields[8]).doubleValue());
				data.setLowPrice(df.parse(fields[9]).doubleValue());

				// 거래량 정보 설정 (정수 값)
				data.setVolume(Long.parseLong(fields[12]));
				data.setAccVolume(Long.parseLong(fields[13]));

				return data;
			} catch (ParseException | NumberFormatException e) {
				log.error("숫자 파싱 실패: {} - {}", e.getMessage(), rawData);
				throw new RuntimeException("데이터 파싱 실패", e);
			}
		} catch (Exception e) {
			log.error("데이터 처리 실패: {} - {}", e.getMessage(), rawData);
			throw e;
		}
	}

}
