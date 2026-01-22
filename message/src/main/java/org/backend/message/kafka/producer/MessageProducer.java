package org.backend.message.kafka.producer;

import org.backend.domain.message.entity.Message;
import org.backend.message.common.dto.MessageSendEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	private static final String TOPIC = "message.send.request";
	
	public void sendMessage(Message message) {
		
		log.info("[ MessageProducer ] - Sending message event for ID : {}", message.getCorrelationId());
		
		// Payload(MessageSendEc
		kafkaTemplate.send(TOPIC
							, message.getId().toString()
							, MessageSendEvent.builder()
										     .messageId(message.getId())
	 										 .dedupKey(message.getDedupKey())
											 .ChannelType(message.getChannelType())
											 .correlationId(message.getCorrelationId())
											 .build());

		log.info("[ MessageProducer ] - Sent message event for ID : {}", message.getCorrelationId());
		
	}
	
	

}
