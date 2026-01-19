package org.backend.message.channel.impl;

import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.MessageSendEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailChannel implements MessageChannel {

	@Override
	public void send(MessageSendEvent message) {
		log.info("[ EmailChannel ] - Sending EMAIL to :{}, Subject: {}");
		
		// 실제 이메일 처리 로직
		
		
		// 이메일 발송 완료 시
		log.info("[ EmailChannel ] - Sent Successfully to :{}");
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.EMAIL;
	}

}
