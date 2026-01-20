package org.backend.message.channel.impl;

import org.backend.core.message.entity.Message;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PushChannel implements MessageChannel {
	
	@Override
	public boolean send(Message message) {
		log.info("[ PushChannel ] - Sending Push to :{}, Subject: {}");
		
		// 실제 푸시 알림 처리 로직
		
		
		// 푸시알림 발송 완료 시
		log.info("[ PushChannel ] - Sent Successfully to :{}");
		
		return true;
		
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.PUSH;
	}

}
