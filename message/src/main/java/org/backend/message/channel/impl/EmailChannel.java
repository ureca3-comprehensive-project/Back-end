package org.backend.message.channel.impl;

import org.backend.core.message.entity.Message;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailChannel implements MessageChannel {
	
	private final double failRate = 0.01;

	@Override
	public boolean send(Message message) {
		log.info("[ EmailChannel ] - Sending EMAIL to :{}, Subject: {}");
		
		// 실제 이메일 처리 로직
		
		try {
			Thread.sleep(1000);
			
			if(Math.random() >= failRate) { // 메시지 발송 성공(99%)
				log.info("[ EmailChannel ] - Sent Successfully to :{}");
				return true;
			}
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("[ EmailChannel ] - Fail to Send :{}");
		return false;
		
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.EMAIL;
	}

}
