package org.backend.message.kafka.outbox;

import java.util.List;

import org.backend.core.message.entity.Message;
import org.backend.core.message.repository.MessageRepository;
import org.backend.message.kafka.producer.MessageProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageOutboxPoller {
	

	

}
