package org.backend.message.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.MessageStatus;
import org.backend.message.kafka.producer.MessageProducer;
import org.springframework.data.domain.PageRequest;
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
	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void poll() {
		
		List<Message> messages = messageRepository.findPendingMessages(List.of(MessageStatus.PENDING, MessageStatus.DND_HOLD),
	            														LocalDateTime.now(),
	            														PageRequest.of(0, 100));
		
		if (messages.isEmpty()) {
			log.info("[ MessageOutboxPoller ] - Pending Message Not Found");
            return;
        }
		
		log.info("[ MessageOutboxPoller ] - Polling {} pending messages", messages.size());
		
		for(Message message : messages) {
			
			try {
				message.markSending();
                messageProducer.sendMessage(message);
                log.debug("[ MessageOutboxPoller ] - Sent message {} to Kafka", message.getId());
            } catch (Exception e) {
                log.error("[ MessageOutboxPoller ] - Failed to send message {} to Kafka", message.getId(), e);
            }

		}
		
	}
	

}
