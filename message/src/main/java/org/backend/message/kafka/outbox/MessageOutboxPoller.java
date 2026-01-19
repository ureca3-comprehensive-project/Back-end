package org.backend.message.kafka.outbox;

import java.util.List;

import org.backend.core.message.entity.Message;
import org.backend.core.message.repository.MessageRepository;
import org.backend.message.kafka.producer.MessageProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageOutboxPoller {
	
	
	private final MessageRepository messageRepository;
	private final MessageProducer messageProducer;
	
	/**
	 * poll() - Message Outbox 스케줄러
	 * 
	 * 
	 */
	@Scheduled(fixedDelay = 3000)
	@Transactional
	public void poll() {
		
		List<Message> messages = messageRepository.findPendingMessages();
		
		if (messages.isEmpty()) {
            return;
        }
		
		log.info("[ MessageOutboxPoller ] - Polling {} pending messages", messages.size());
		
		for(Message message : messages) {
			
			try {
                messageProducer.sendMessage(message);
                log.debug("[ MessageOutboxPoller ] - Sent message {} to Kafka", message.getId());
            } catch (Exception e) {
                log.error("[ MessageOutboxPoller ] - Failed to send message {} to Kafka", message.getId(), e);
            }

		}
		
	}
	

}
