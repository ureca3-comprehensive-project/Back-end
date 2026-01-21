package org.backend.message.unit.kafka;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.common.dto.MessageSendEvent;
import org.backend.message.dispatcher.MessageDispatcher;
import org.backend.message.kafka.consumer.MessageSendConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageSendConsumer 테스트")
public class MessageSendConsumerUnitTest {
	
	@Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageDispatcher dispatcher;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private MessageSendConsumer messageSendConsumer;

    @Nested
    @DisplayName("consumeMessageEvent 메서드는")
    class ConsumeMessageEvent {

        @Test
        @DisplayName("이벤트를 정상적으로 처리하고 ACK를 전송한다")
        void processEventAndAcknowledge() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doNothing().when(dispatcher).dispatch(1L);

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            verify(dispatcher).dispatch(1L);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("dispatcher를 올바른 messageId로 호출한다")
        void callDispatcherWithCorrectMessageId() {
            // given
            MessageSendEvent event = createEvent(100L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doNothing().when(dispatcher).dispatch(100L);

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            verify(dispatcher).dispatch(100L);
        }

        @Test
        @DisplayName("dispatcher 호출 후 ACK를 전송한다")
        void acknowledgeAfterDispatch() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            var inOrder = inOrder(dispatcher, acknowledgment);
            inOrder.verify(dispatcher).dispatch(1L);
            inOrder.verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("dispatcher에서 예외 발생 시 ACK를 전송하지 않는다")
        void doNotAcknowledgeWhenDispatcherThrowsException() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doThrow(new RuntimeException("Dispatch failed"))
                .when(dispatcher).dispatch(1L);

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            verify(dispatcher).dispatch(1L);
            verify(acknowledgment, never()).acknowledge();
        }

        @Test
        @DisplayName("예외 발생해도 전체 프로세스가 중단되지 않는다")
        void handleExceptionGracefully() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doThrow(new RuntimeException("Error"))
                .when(dispatcher).dispatch(1L);

            // when & then
            assertThatCode(() -> messageSendConsumer.consumeMessageEvent(event, acknowledgment))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("NullPointerException 발생 시에도 ACK를 전송하지 않는다")
        void doNotAcknowledgeOnNullPointerException() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doThrow(new NullPointerException("Null value"))
                .when(dispatcher).dispatch(1L);

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            verify(acknowledgment, never()).acknowledge();
        }

        @Test
        @DisplayName("IllegalArgumentException 발생 시에도 ACK를 전송하지 않는다")
        void doNotAcknowledgeOnIllegalArgumentException() {
            // given
            MessageSendEvent event = createEvent(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
            
            doThrow(new IllegalArgumentException("Invalid argument"))
                .when(dispatcher).dispatch(1L);

            // when
            messageSendConsumer.consumeMessageEvent(event, acknowledgment);

            // then
            verify(acknowledgment, never()).acknowledge();
        }
    }
	
	
	
	// Util method
    private MessageSendEvent createEvent(Long messageId, String dedupKey, 
                                        ChannelType channelType, String correlationId) {
        return MessageSendEvent.builder()
            .messageId(messageId)
            .dedupKey(dedupKey)
            .ChannelType(channelType)
            .correlationId(correlationId)
            .build();
    }

    // Custom Exception for testing
    static class CustomDispatchException extends RuntimeException {
        public CustomDispatchException(String message) {
            super(message);
        }
    }

}
