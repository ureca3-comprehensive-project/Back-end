package org.backend.message.unit.dispatcher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.backend.core.message.entity.Message;
import org.backend.core.message.entity.MessageAttempt;
import org.backend.core.message.repository.MessageAttemptRepository;
import org.backend.core.message.repository.MessageRepository;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.backend.message.dispatcher.MessageDispatcher;
import org.backend.message.policy.DndPolicy;
import org.backend.message.policy.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageDispatcher 테스트")
public class MessageDispatcherUnitTest {
	
	 @Mock
	    private MessageRepository messageRepository;

	    @Mock
	    private MessageAttemptRepository messageAttemptRepository;

	    @Mock
	    private DndPolicy dndPolicy;

	    @Mock
	    private RetryPolicy retryPolicy;

	    @Mock
	    private MessageChannel emailChannel;

	    @Mock
	    private MessageChannel smsChannel;

	    @Mock
	    private MessageChannel pushChannel;

	    @InjectMocks
	    private MessageDispatcher messageDispatcher;

	    @Captor
	    private ArgumentCaptor<MessageAttempt> attemptCaptor;

	    private List<MessageChannel> channels;

	    @BeforeEach
	    void setUp() {
	        channels = Arrays.asList(emailChannel, smsChannel, pushChannel);
	        
	        // channels 리플렉션 주입
	        try {
	            var field = MessageDispatcher.class.getDeclaredField("channels");
	            field.setAccessible(true);
	            field.set(messageDispatcher, channels);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }

	        
	        lenient().when(emailChannel.supports(ChannelType.EMAIL)).thenReturn(true);
	        lenient().when(emailChannel.supports(ChannelType.SMS)).thenReturn(false);
	        lenient().when(emailChannel.supports(ChannelType.PUSH)).thenReturn(false);
	        
	        lenient().when(smsChannel.supports(ChannelType.EMAIL)).thenReturn(false);
	        lenient().when(smsChannel.supports(ChannelType.SMS)).thenReturn(true);
	        lenient().when(smsChannel.supports(ChannelType.PUSH)).thenReturn(false);
	        
	        lenient().when(pushChannel.supports(ChannelType.EMAIL)).thenReturn(false);
	        lenient().when(pushChannel.supports(ChannelType.SMS)).thenReturn(false);
	        lenient().when(pushChannel.supports(ChannelType.PUSH)).thenReturn(true);
	    }

	    @Nested
	    @DisplayName("dispatch 메서드는")
	    class Dispatch {

	        @Test
	        @DisplayName("메시지를 찾을 수 없으면 예외를 던진다")
	        void throwExceptionWhenMessageNotFound() {
	            // given
	            given(messageRepository.findById(999L))
	                .willReturn(Optional.empty());

	            // when & then
	            assertThatThrownBy(() -> messageDispatcher.dispatch(999L))
	                .isInstanceOf(IllegalArgumentException.class);
	        }

	        @Test
	        @DisplayName("메시지를 SENDING 상태로 변경한다")
	        void markMessageAsSending() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.send(message)).willReturn(ChannelSendResult.success("msg-123", 200));

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(message).markSending();
	        }

	        @Test
	        @DisplayName("DND 시간대에는 메시지를 보류한다")
	        void holdMessageDuringDnd() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            LocalDateTime dndEndTime = LocalDateTime.now().plusHours(8);
	            LocalDateTime availableAt = LocalDateTime.now();
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(true);
	            given(dndPolicy.nextAvailableTime(message)).willReturn(dndEndTime);
	            when(message.getAvailableAt()).thenReturn(availableAt);

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(message).dndHold(dndEndTime);
	            verify(emailChannel, never()).send(any());
	        }

	        @Test
	        @DisplayName("DND 종료 시간과 예약 시간 중 더 늦은 시간으로 조정한다")
	        void adjustToLaterTimeBetweenDndAndScheduled() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            LocalDateTime dndEndTime = LocalDateTime.now().plusHours(8);
	            LocalDateTime scheduledTime = LocalDateTime.now().plusHours(10); // DND보다 늦음
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(true);
	            given(dndPolicy.nextAvailableTime(message)).willReturn(dndEndTime);
	            when(message.getAvailableAt()).thenReturn(scheduledTime);

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(message).dndHold(scheduledTime); // 더 늦은 예약 시간으로 설정
	        }

	        @Test
	        @DisplayName("발송 성공 시 MessageAttempt를 저장하고 메시지를 SENT로 변경한다")
	        void saveAttemptAndMarkSentOnSuccess() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            ChannelSendResult successResult = ChannelSendResult.success("provider-msg-123", 200);
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.send(message)).willReturn(successResult);

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(messageAttemptRepository).save(any(MessageAttempt.class));
	            verify(message).markSent();
	        }

	        @Test
	        @DisplayName("발송 실패 시 재시도 횟수를 증가시킨다")
	        void increaseRetryCountOnFailure() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            ChannelSendResult failResult = ChannelSendResult.fail("ERR001", "Network error", 500);
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.send(message)).willReturn(failResult);
	            given(retryPolicy.canRetry(message)).willReturn(true);

	            // when & then
	            assertThatThrownBy(() -> messageDispatcher.dispatch(1L))
	                .isInstanceOf(RuntimeException.class)
	                .hasMessage("retry");
	            
	            verify(message).increaseRetry();
	        }

	        @Test
	        @DisplayName("재시도 가능하면 RuntimeException을 던진다")
	        void throwExceptionWhenRetryAvailable() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            ChannelSendResult failResult = ChannelSendResult.fail("ERR001", "Temporary error", 503);
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.send(message)).willReturn(failResult);
	            given(retryPolicy.canRetry(message)).willReturn(true);

	            // when & then
	            assertThatThrownBy(() -> messageDispatcher.dispatch(1L))
	                .isInstanceOf(RuntimeException.class)
	                .hasMessage("retry");
	        }

	        @Test
	        @DisplayName("재시도 불가하고 다음 채널이 있으면 채널을 변경한다")
	        void switchToNextChannelWhenRetryNotAvailable() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            ChannelSendResult failResult = ChannelSendResult.fail("ERR001", "Failed", 500);
	            ChannelType nextChannel = ChannelType.SMS;
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.send(message)).willReturn(failResult);
	            given(retryPolicy.canRetry(message)).willReturn(false);

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(message).increaseRetry();
	            verify(message).switchChannel(any(ChannelType.class));
	            verify(message).markPending();
	        }

	        @Test
	        @DisplayName("재시도 불가하고 다음 채널이 없으면 실패 처리한다")
	        void markFailWhenNoChannelSupportedType() {
	            // given
	            Message message = createMessage(1L, ChannelType.PUSH);
	            ChannelSendResult failResult = ChannelSendResult.fail("ERR001", "Failed", 500);
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(pushChannel.send(message)).willReturn(failResult);
	            given(retryPolicy.canRetry(message)).willReturn(false);

	            // when
	            messageDispatcher.dispatch(1L);

	            // then
	            verify(message).increaseRetry();
	            verify(message).markFail();
	            verify(message, never()).switchChannel(any());
	        }
	        
	        @Test
	        @DisplayName("지원하지 않는 ChannelType 이라면 예외를 발생시킨다")
	        void throwExceptionWhenNoNextChannel() {
	            // given
	            Message message = createMessage(1L, ChannelType.EMAIL);
	            ChannelSendResult failResult = ChannelSendResult.fail("ERR001", "Failed", 500);
	            
	            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
	            given(dndPolicy.isDndNow(message)).willReturn(false);
	            given(emailChannel.supports(ChannelType.EMAIL)).willReturn(false);
	            given(smsChannel.supports(ChannelType.EMAIL)).willReturn(false);

	            // when & then
	            assertThatThrownBy(() -> messageDispatcher.dispatch(1L))
	            .isInstanceOf(IllegalArgumentException.class)
	            .hasMessageContaining("No channel found");
	        }
	    }
	
	
	
	// Util method
    private Message createMessage(Long id, ChannelType channelType) {
    	Message message = mock(Message.class);
        lenient().when(message.getId()).thenReturn(id);
        lenient().when(message.getChannelType()).thenReturn(channelType);
        lenient().when(message.getRetryCount()).thenReturn(0);
        lenient().when(message.getAvailableAt()).thenReturn(LocalDateTime.now());
        return message;
    }

}
