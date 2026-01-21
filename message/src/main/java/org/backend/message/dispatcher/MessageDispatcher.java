package org.backend.message.dispatcher;

import java.time.LocalDateTime;
import java.util.List;

import org.backend.core.message.entity.Message;
import org.backend.core.message.entity.MessageAttempt;
import org.backend.core.message.repository.MessageAttemptRepository;
import org.backend.core.message.repository.MessageRepository;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.backend.message.common.util.TemplateBuilder;
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
	private final MessageAttemptRepository messageAttemptRepository;
	private final DndPolicy dndPolicy;
    private final RetryPolicy retryPolicy;
    
    private final List<MessageChannel> channels;
    
    
	
	@Transactional
    public void dispatch(Long messageId) {
		
		Message message = messageRepository.findById(messageId).orElseThrow(
					() -> new IllegalArgumentException()
				);
		
		
		message.markSending();
		
		
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
        String payload = TemplateBuilder.build();
        
        MessageAttempt attempt =
                MessageAttempt.attempting(message, message.getRetryCount() + 1, payload);
        
        messageAttemptRepository.save(attempt);
        
        MessageChannel channel = findChannel(message.getChannelType());
        ChannelSendResult sendResult = channel.send(message);
        
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
        	throw new RuntimeException("retry");
        }
        
        //4. 실패 처리
        ChannelType next = message.getChannelType().next();

        if (next != null) {
            message.switchChannel(next);
            message.markPending();
        } else {
            message.markFail();
        }
        
        
		
	}
	
	
	private MessageChannel findChannel(ChannelType channelType) {
		return channels.stream()
				       .filter(channel -> channel.supports(channelType))
				       .findFirst()
			           .orElseThrow(() -> new IllegalArgumentException("No channel found for: " + channelType));
	}

}
