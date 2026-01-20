package org.backend.message.policy;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.backend.core.message.entity.Message;
import org.backend.core.user.entity.BanTime;
import org.backend.core.user.repository.BanTimeRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DndPolicy{
	
	private final BanTimeRepository banTimeRepository;
	
	
	public boolean isDndNow(Message message) {
		
		BanTime dnd = banTimeRepository.findBanTimeByMessageId(message.getId()).orElseThrow(
					() -> new IllegalArgumentException("BenTime not found : " + message.getId())
				);
		
        LocalTime now = LocalTime.now();
        
        return now.isAfter(dnd.getStartTime()) || now.isBefore(dnd.getEndTime());
        
    }

    public LocalDateTime nextAvailableTime(Message message) {
    	
    	BanTime dnd = banTimeRepository.findBanTimeByMessageId(message.getId()).orElseThrow(
    				() -> new IllegalArgumentException("BenTime not found : " + message.getId())
                );
    	
    	
    	LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = dnd.getStartTime();
        LocalTime endTime = dnd.getEndTime();
    	
    	if (!isDndNow(message)) {
            return now;
        }
    	
    	LocalDateTime nextTime = now.with(endTime);
    	
    	if (nextTime.isBefore(now)) {
            nextTime = nextTime.plusDays(1);
        }

        return nextTime;
    }



}
