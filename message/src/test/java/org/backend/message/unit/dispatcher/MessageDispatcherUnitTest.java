package org.backend.message.unit.dispatcher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.backend.core.dto.InvoiceDto;
import org.backend.core.util.template.TemplateUtil;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.repository.MessageAttemptRepository;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.template.entity.Template;
import org.backend.domain.template.repository.TemplateRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageDispatcher 단위 테스트")
public class MessageDispatcherUnitTest {

    @Mock private MessageRepository messageRepository;
    @Mock private MessageAttemptRepository messageAttemptRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private DndPolicy dndPolicy;
    @Mock private RetryPolicy retryPolicy;
    @Mock private TemplateUtil templateUtil;

    // 리스트 주입을 위해 Spy와 실제 리스트 사용
    @Mock private MessageChannel emailChannel;
    @Mock private MessageChannel smsChannel;
    @Spy private List<MessageChannel> channels = new ArrayList<>();

    @InjectMocks
    private MessageDispatcher messageDispatcher;

    @BeforeEach
    void setUp() {
        channels.clear();
        channels.add(emailChannel);
        channels.add(smsChannel);

        lenient().when(emailChannel.supports(ChannelType.EMAIL)).thenReturn(true);
        lenient().when(smsChannel.supports(ChannelType.SMS)).thenReturn(true);
    }

    @Nested
    @DisplayName("dispatch 메서드는")
    class Dispatch {

        @Test
        @DisplayName("DND 시간대일 경우 발송을 중단하고 시간을 조정한다")
        void holdMessageDuringDnd() {
            // given
            Message message = mock(Message.class);
            LocalDateTime dndEndTime = LocalDateTime.now().plusHours(1);
            
            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
            given(dndPolicy.isDndNow(message)).willReturn(true);
            given(dndPolicy.nextAvailableTime(message)).willReturn(dndEndTime);
            given(message.getAvailableAt()).willReturn(LocalDateTime.now());

            // when
            messageDispatcher.dispatch(1L);

            // then
            verify(message).dndHold(any(LocalDateTime.now().getClass()));
            verify(emailChannel, never()).send(any());
        }

        @Test
        @DisplayName("발송 성공 시 시도 이력을 저장하고 상태를 SENT로 변경한다")
        void successProcess() {
            // given
            Message message = mock(Message.class);
            Invoice invoice = mock(Invoice.class);
            MessageAttempt attempt = mock(MessageAttempt.class);
            
            // [추가] Invoice의 필드들이 null을 반환하지 않도록 설정
            given(invoice.getId()).willReturn(1L);
            given(invoice.getTotalAmount()).willReturn(BigDecimal.valueOf(50000)); // longValue() 호출 대비
            given(invoice.getBillingMonth()).willReturn("2023-12");
            given(invoice.getDueDate()).willReturn(LocalDateTime.now()); // toLocalDate() 호출 대비
            given(invoice.getDetails()).willReturn(new ArrayList<>()); // stream() 호출 대비

            given(messageRepository.findById(1L)).willReturn(Optional.of(message));
            given(message.getChannelType()).willReturn(ChannelType.EMAIL);
            given(message.getInvoice()).willReturn(invoice);
            given(message.getCorrelationId()).willReturn("corr-123");
            
            // MessageAttempt 생성 및 저장 Mock
            given(messageAttemptRepository.countByMessage_CorrelationId(any())).willReturn(0L);
            given(messageAttemptRepository.save(any())).willReturn(attempt);
            
            ChannelSendResult result = ChannelSendResult.success("provider-msg-123", 200);
            given(emailChannel.send(any())).willReturn(result);

            // when
            messageDispatcher.dispatch(1L);

            // then
            verify(attempt).success("provider-msg-123", 200);
            verify(message).markSent();
        }

        @Test
        @DisplayName("발송 실패 시 재시도 가능하면 예외를 던져 트랜잭션을 롤백 유도한다")
        void retryProcess() {
            // given
            Message message = mock(Message.class);
            setupCommonFailMock(message);
            
            given(retryPolicy.canRetry(message)).willReturn(true);
            given(emailChannel.send(any())).willReturn(ChannelSendResult.fail("F1", "Error", 500));

            // when & then
            assertThatThrownBy(() -> messageDispatcher.dispatch(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("retry");
            
            verify(message).increaseRetry();
            verify(message).markPending();
        }

        @Test
        @DisplayName("재시도 불가하고 다음 채널이 있으면 다음 채널 메시지를 생성한다")
        void nextChannelProcess() {
            // given
            Message message = mock(Message.class);
            setupCommonFailMock(message);
            
            given(retryPolicy.canRetry(message)).willReturn(false); // 재시도 끝
            given(emailChannel.send(any())).willReturn(ChannelSendResult.fail("F1", "Error", 500));
            
            // EMAIL 다음은 SMS라고 가정 (Enum 구조에 따라 다름)
            given(templateRepository.findTop1ByTypeOrderByUpdatedAtDesc(any()))
                .willReturn(Optional.of(mock(Template.class)));

            // when
            messageDispatcher.dispatch(1L);

            // then
            verify(messageRepository).save(any(Message.class)); // 다음 채널용 새 메시지 저장
            verify(message).markFail(); // 기존 메시지는 실패 처리
        }
    }

    private void setupCommonFailMock(Message message) {
        Invoice invoice = mock(Invoice.class);
        
        // 추가: BigDecimal longValue() 호출 시 NPE 방지를 위해 Mock 설정
        given(invoice.getTotalAmount()).willReturn(BigDecimal.ZERO);
        given(invoice.getBillingMonth()).willReturn("2023-10");
        // InvoiceDto.builder()에서 dueDate.toLocalDate()를 호출하므로 LocalDateTime 설정 필요
        given(invoice.getDueDate()).willReturn(LocalDateTime.now()); 
        
        given(messageRepository.findById(1L)).willReturn(Optional.of(message));
        given(message.getChannelType()).willReturn(ChannelType.EMAIL);
        given(message.getInvoice()).willReturn(invoice);
        given(invoice.getDetails()).willReturn(new ArrayList<>());
        
        // MessageAttempt 생성 시 payloadJson이 필요하므로 Mock 설정
        given(messageAttemptRepository.save(any())).willReturn(mock(MessageAttempt.class));
    }
}