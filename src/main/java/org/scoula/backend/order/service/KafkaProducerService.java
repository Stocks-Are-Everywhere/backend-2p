package org.scoula.backend.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaProducerService {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	private static final String TOPIC = "my-topic";

	public void sendMessage(String message) {
		log.info("producer 도착!");
		kafkaTemplate.send(TOPIC, message);
	}
}
