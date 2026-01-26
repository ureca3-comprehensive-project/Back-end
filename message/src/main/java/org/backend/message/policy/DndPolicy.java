package org.backend.message.policy;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.backend.domain.message.entity.Message;
import org.backend.domain.user.entity.BanTime;
import org.backend.domain.user.repository.BanTimeRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DndPolicy{
	
	private final BanTimeRepository banTimeRepository;
	
	
	public boolean isDndNow(Message message) {
		
		BanTime dnd = banTimeRepository.findBanTimeByMessageId(message.getId())
	            .orElseThrow(() -> new IllegalArgumentException("BanTime not found : " + message.getId()));

	    LocalTime now = LocalTime.now();
	    LocalTime start = dnd.getStartTime();
	    LocalTime end = dnd.getEndTime();

	    if (start.isBefore(end)) {
	        // 일반적인 케이스 (예: 09:00 ~ 18:00)
	        return now.isAfter(start) && now.isBefore(end);
	    } else {
	        // 자정을 넘기는 케이스 (예: 22:00 ~ 08:00)
	        // 현재 시간이 시작 이후이거나 종료 이전이면 DND임
	        return now.isAfter(start) || now.isBefore(end);
	    }
        
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
    	
    	if (nextTime.isAfter(now)) {
            nextTime = nextTime.plusDays(1);
        }
    	
        return nextTime;
    }



}
