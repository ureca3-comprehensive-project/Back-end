package org.backend.message.unit.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.common.dto.MessageSendEvent;
import org.backend.message.kafka.producer.MessageProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageProducer 테스트")
public class MessageProducerUnitTest {
	
	 @Mock
	    private KafkaTemplate<String, Object> kafkaTemplate;

	    @InjectMocks
	    private MessageProducer messageProducer;

	    @Captor
	    private ArgumentCaptor<String> topicCaptor;

	    @Captor
	    private ArgumentCaptor<String> keyCaptor;

	    @Captor
	    private ArgumentCaptor<MessageSendEvent> eventCaptor;

	    private static final String TOPIC = "message.send.request";

	    @Nested
	    @DisplayName("sendMessage 메서드는")
	    class SendMessage {

	        @Test
	        @DisplayName("Message를 Kafka로 전송한다")
	        void sendMessageToKafka() {
	            // given
	            Message message = createMessage(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
	            
	            given(kafkaTemplate.send(anyString(), anyString(), any()))
	                .willReturn(new CompletableFuture<>());

	            // when
	            messageProducer.sendMessage(message);

	            // then
	            verify(kafkaTemplate).send(
	                eq(TOPIC),
	                eq("1"),
	                any(MessageSendEvent.class)
	            );
	        }

	        @Test
	        @DisplayName("올바른 토픽으로 메시지를 전송한다")
	        void sendToCorrectTopic() {
	            // given
	            Message message = createMessage(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
	            
	            given(kafkaTemplate.send(anyString(), anyString(), any()))
	                .willReturn(new CompletableFuture<>());

	            // when
	            messageProducer.sendMessage(message);

	            // then
	            verify(kafkaTemplate).send(
	                topicCaptor.capture(),
	                anyString(),
	                any()
	            );
	            
	            assertThat(topicCaptor.getValue()).isEqualTo(TOPIC);
	        }

	        @Test
	        @DisplayName("메시지 ID를 Key로 사용한다")
	        void useMessageIdAsKey() {
	            // given
	            Message message = createMessage(100L, "dedup-123", ChannelType.EMAIL, "corr-456");
	            
	            given(kafkaTemplate.send(anyString(), anyString(), any()))
	                .willReturn(new CompletableFuture<>());

	            // when
	            messageProducer.sendMessage(message);

	            // then
	            verify(kafkaTemplate).send(
	                anyString(),
	                keyCaptor.capture(),
	                any()
	            );
	            
	            assertThat(keyCaptor.getValue()).isEqualTo("100");
	        }

	        @Test
	        @DisplayName("MessageSendEvent를 올바르게 생성하여 전송한다")
	        void sendCorrectMessageSendEvent() {
	            // given
	            Message message = createMessage(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
	            
	            given(kafkaTemplate.send(anyString(), anyString(), any()))
	                .willReturn(new CompletableFuture<>());

	            // when
	            messageProducer.sendMessage(message);

	            // then
	            verify(kafkaTemplate).send(
	                anyString(),
	                anyString(),
	                eventCaptor.capture()
	            );
	            
	            MessageSendEvent event = eventCaptor.getValue();
	            assertThat(event).isNotNull();
	            assertThat(event.getMessageId()).isEqualTo(1L);
	            assertThat(event.getDedupKey()).isEqualTo("dedup-123");
	            assertThat(event.getChannelType()).isEqualTo(ChannelType.EMAIL);
	            assertThat(event.getCorrelationId()).isEqualTo("corr-456");
	        }

	        @Test
	        @DisplayName("Kafka 전송 실패 시 예외를 전파한다")
	        void propagateExceptionWhenKafkaFails() {
	            // given
	            Message message = createMessage(1L, "dedup-123", ChannelType.EMAIL, "corr-456");
	            
	            given(kafkaTemplate.send(anyString(), anyString(), any()))
	                .willThrow(new RuntimeException("Kafka connection failed"));

	            // when & then
	            assertThatThrownBy(() -> messageProducer.sendMessage(message))
	                .isInstanceOf(RuntimeException.class)
	                .hasMessageContaining("Kafka connection failed");
	        }
	    }
	
	
	// Helper method
    private Message createMessage(Long id, String dedupKey, ChannelType channelType, String correlationId) {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(id);
        when(message.getDedupKey()).thenReturn(dedupKey);
        when(message.getChannelType()).thenReturn(channelType);
        when(message.getCorrelationId()).thenReturn(correlationId);
        return message;
    }

}
