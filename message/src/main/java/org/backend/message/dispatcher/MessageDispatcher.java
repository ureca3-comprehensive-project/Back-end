package org.backend.message.dispatcher;

import java.util.List;

import org.backend.core.message.entity.Message;
import org.backend.core.message.repository.MessageRepository;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
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
        	// DND 시간대에 걸리면 
        	message.dndHold(dndPolicy.nextAvailableTime(message));
        	return;
        }
        
        // 2. 채널 발송
        MessageChannel channel = findChannel(message.getChannelType());
        boolean sendResult = channel.send(message);
        
        if (sendResult) {
            message.markSent();
            return;
        }
        
        // 3. 재시도 처리
        message.increaseRetry();
        
        if (retryPolicy.canRetry(message)) { // 재시도 횟수가 남아있다면
        	throw new RuntimeException("retry");
        }
        
        ChannelType next = message.getChannelType().next();
        if (next != null) {
            message.switchChannel(next);
            message.markPending();
        } else {
        	// 4. 실패 처리
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
