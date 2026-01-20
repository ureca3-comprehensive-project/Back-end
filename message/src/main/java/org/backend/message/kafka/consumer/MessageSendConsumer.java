package org.backend.message.kafka.consumer;

import org.backend.core.message.repository.MessageRepository;
import org.backend.message.common.dto.MessageSendEvent;
import org.backend.message.dispatcher.MessageDispatcher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import io.lettuce.core.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSendConsumer {
	
	private final MessageRepository messageRepository;
    private final MessageDispatcher dispatcher;
	
	@KafkaListener(
        topics = "message.send.request"
    )
    public void consumeMessageEvent(MessageSendEvent event, Acknowledgment ack) {
		
		try {
            Long messageId = event.getMessageId();
            log.info("Consuming message event for ID: {}", messageId);
            
            dispatcher.dispatch(messageId);
            
            // 수동 커밋
            ack.acknowledge();
            log.debug("Message {} processed and acknowledged", messageId);
            
        } catch (Exception e) {
            log.error("Error processing message event", e);
            // 재시도 로직이 필요한 경우 ack를 하지 않음
        }
		
		
	}

}
