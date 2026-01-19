package org.backend.message.channel.impl;

import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.MessageSendEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SMSChannel implements MessageChannel {

	@Override
	public void send(MessageSendEvent message) {
		log.info("[ SMSChannel ] - Sending SMS to :{}, Subject: {}");
		
		// 실제 푸시 알림 처리 로직
		
		
		// 푸시알림 발송 완료 시
		log.info("[ SMSChannel ] - Sent Successfully to :{}");		
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.SMS;
	}

}
