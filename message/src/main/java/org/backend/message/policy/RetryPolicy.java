package org.backend.message.policy;

import org.backend.domain.message.entity.Message;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RetryPolicy {
	
	
    public boolean canRetry(Message message) {
        return message.getRetryCount() < message.getMaxRetry();
    }

}
