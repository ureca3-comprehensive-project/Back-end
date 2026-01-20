package org.backend.message.unit.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.backend.core.message.entity.Message;
import org.backend.core.message.repository.MessageRepository;
import org.backend.core.message.type.ChannelType;
import org.backend.core.message.type.MessageStatus;
import org.backend.message.kafka.outbox.MessageOutboxPoller;
import org.backend.message.kafka.producer.MessageProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageOutboxPoller 테스트")
public class MessageOutboxPollerUnitTest {
	
	@Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private MessageOutboxPoller messageOutboxPoller;
    
    
    @Nested
    @DisplayName("poll 메서드는")
    class Poll {

        @Test
        @DisplayName("Pending 메시지가 없으면 Kafka로 전송하지 않는다")
        void doNothingWhenNoPendingMessages() {
            // given
            given(messageRepository.findPendingMessages( anyList(),
                    any(LocalDateTime.class),
                    any(Pageable.class)))
                .willReturn(Collections.emptyList());

            // when
            messageOutboxPoller.poll();

            // then
            verify(messageRepository).findPendingMessages(
            	    eq(List.of(MessageStatus.PENDING, MessageStatus.DND_HOLD)),
            	    any(LocalDateTime.class),
            	    eq(PageRequest.of(0, 100))
            	);
            verify(messageProducer, never()).sendMessage(any());
        }

        @Test
        @DisplayName("Pending 메시지 1개를 Kafka로 전송한다")
        void sendSinglePendingMessage() {
            // given
            Message message = createMessage(1L);
            given(messageRepository.findPendingMessages( anyList(),
                    any(LocalDateTime.class),
                    any(Pageable.class)))
                .willReturn(List.of(message));

            // when
            messageOutboxPoller.poll();

            // then
            verify(messageRepository).findPendingMessages(
            	    eq(List.of(MessageStatus.PENDING, MessageStatus.DND_HOLD)),
            	    any(LocalDateTime.class),
            	    eq(PageRequest.of(0, 100))
            	);
            verify(messageProducer).sendMessage(message);
        }

        @Test
        @DisplayName("여러 Pending 메시지를 Kafka로 전송한다")
        void sendMultiplePendingMessages() {
            // given
            Message message1 = createMessage(1L);
            Message message2 = createMessage(2L);
            Message message3 = createMessage(3L);
            
            given(messageRepository.findPendingMessages( anyList(),
                    any(LocalDateTime.class),
                    any(Pageable.class)))
                .willReturn(Arrays.asList(message1, message2, message3));

            // when
            messageOutboxPoller.poll();

            // then
            verify(messageRepository).findPendingMessages(
            	    eq(List.of(MessageStatus.PENDING, MessageStatus.DND_HOLD)),
            	    any(LocalDateTime.class),
            	    eq(PageRequest.of(0, 100))
            	);
            verify(messageProducer).sendMessage(message1);
            verify(messageProducer).sendMessage(message2);
            verify(messageProducer).sendMessage(message3);
        }
        
        @Test
        @DisplayName("메시지 전송 중 예외가 발생해도 계속 처리한다")
        void continueProcessingWhenExceptionOccurs() {
            // given
            Message message1 = createMessage(1L);
            Message message2 = createMessage(2L);
            Message message3 = createMessage(3L);
            
            given(messageRepository.findPendingMessages( anyList(),
                    any(LocalDateTime.class),
                    any(Pageable.class)))
                .willReturn(Arrays.asList(message1, message2, message3));
            
            // message2 전송 시 예외 발생
            doNothing().when(messageProducer).sendMessage(message1);
            doThrow(new RuntimeException("Kafka error"))
                .when(messageProducer).sendMessage(message2);
            doNothing().when(messageProducer).sendMessage(message3);

            // when
            messageOutboxPoller.poll();

            // then
            verify(messageProducer).sendMessage(message1);
            verify(messageProducer).sendMessage(message2);
            verify(messageProducer).sendMessage(message3); // 예외 발생해도 계속 진행
        }
    }
    
    
 // Helper methods
    private Message createMessage(Long id) {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(id);
        return message;
    }

    private Message createMessageWithType(Long id, ChannelType type) {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(id);
        when(message.getChannelType()).thenReturn(type);
        return message;
    }

}
