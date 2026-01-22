package org.backend.message.dispatcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.backend.core.dto.InvoiceDetailDto;
import org.backend.core.dto.InvoiceDto;
import org.backend.core.util.message.DedeupKeyUtil;
import org.backend.core.util.template.TemplateUtil;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.entity.InvoiceDetail;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.repository.MessageAttemptRepository;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.message.type.MessageStatus;
import org.backend.domain.template.repository.TemplateRepository;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.backend.message.common.util.PayloadJsonUtil;
import org.backend.message.policy.DndPolicy;
import org.backend.message.policy.RetryPolicy;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcher {
	
	private final MessageRepository messageRepository;
	private final TemplateRepository templateRepository;
	private final MessageAttemptRepository messageAttemptRepository;
	private final DndPolicy dndPolicy;
    private final RetryPolicy retryPolicy;
    
    private final List<MessageChannel> channels;
    private final TemplateUtil templateUtil;
    
    
	
	@Transactional
    public void dispatch(Long messageId) {
		
		Message message = messageRepository.findById(messageId).orElseThrow(
					() -> new IllegalArgumentException()
				);
		
		
		// 1. DND Check
        if (dndPolicy.isDndNow(message)) {
        	
        	LocalDateTime dndEnd = dndPolicy.nextAvailableTime(message);
        	
        	// 예약 발송 건에 대해 DND에 걸릴 경우 DND 종료 시간과 예약 시간 중 더 늦은 시간을 기준으로 재발송
        	LocalDateTime adjustedTime =
                    message.getAvailableAt().isAfter(dndEnd) ? message.getAvailableAt() : dndEnd;
        	 
        	message.dndHold(adjustedTime);
        	return;
        }
        
        // 2. 채널 발송
        
        // 템플릿의 가변 변수를 사용하여 제목 및 본문 작성 부분
        List<InvoiceDetailDto> detailDtos =
                message.getInvoice().getDetails().stream()
                              .map(this::toInvoiceDetailDto)
                              .toList();
        
        Map<String, Object> payload = 
        		templateUtil.extractInvoicePayload(toInvoiceDto(message.getInvoice()),detailDtos);

        String payloadJson = PayloadJsonUtil.toJson(payload);
        
        MessageAttempt attempt =
                MessageAttempt.attempting(message, 
                						  messageAttemptRepository.countByMessage_CorrelationId(message.getCorrelationId()) + 1, 
                						  payloadJson);
        
        attempt = messageAttemptRepository.save(attempt);
        
        MessageChannel channel = findChannel(message.getChannelType());
        ChannelSendResult sendResult = channel.send(attempt);
        
        if (sendResult.isSuccess()) {
        	
        	attempt.success(
        			sendResult.getProviderMessageId(),
        			sendResult.getHttpStatus()
                );
        	
            message.markSent();
            return;
        }
        
        
        attempt.fail(
        		sendResult.getFailCode(),
        		sendResult.getFailReason(),
        		sendResult.getHttpStatus()
            );
        
        // 3. 재시도 처리
        message.increaseRetry();
        
        if (retryPolicy.canRetry(message)) { // 재시도 횟수가 남아있다면
        	message.markPending();
        	throw new RuntimeException("retry");
        }
        
        //4. 실패 처리
        ChannelType next = message.getChannelType().next();

        if (next != null) { // 다음 전송 채널이 있으면
        	
        	Message nextMessage = Message.builder()
        								 .channelType(next)
        								 .status(MessageStatus.PENDING)
        								 .dedupKey(DedeupKeyUtil.generate(message.getId(), next.name()))
        								 .correlationId(message.getCorrelationId())
        								 .invoice(message.getInvoice())
        								 .template(templateRepository.findTop1ByTypeOrderByUpdatedAtDesc(next).get())
        								 .build();
        	
        	messageRepository.save(nextMessage);
        	
        }
        
        message.markFail();
		
	}
	
	
	private MessageChannel findChannel(ChannelType channelType) {
		return channels.stream()
				       .filter(channel -> channel.supports(channelType))
				       .findFirst()
			           .orElseThrow(() -> new IllegalArgumentException("No channel found for: " + channelType));
	}
	
	
	private InvoiceDto toInvoiceDto(Invoice invoice) {
		return InvoiceDto.builder()
						 .id(invoice.getId())
						 .billingMonth(invoice.getBillingMonth())
						 .totalAmount(invoice.getTotalAmount().longValue())
						 .dueDate(invoice.getDueDate().toLocalDate())
						 .build();
	}
	
	private InvoiceDetailDto toInvoiceDetailDto(InvoiceDetail detail) {
		return InvoiceDetailDto.builder()
							   .billingType(detail.getBillingType())
							   .amount(detail.getAmount().longValue())
//							   .positive(detail.get) /*InvoiceDetail의 status는 어떤 값이 들어가는가*/
							   .build();
	}

}
