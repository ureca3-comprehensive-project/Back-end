package org.backend.message.channel.impl;

import org.backend.core.message.entity.Message;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailChannel implements MessageChannel {
	
	private final double failRate = 0.01;

	@Override
	public ChannelSendResult send(Message message) {
		log.info("[ EmailChannel ] - Sending EMAIL to :{}, Subject: {}");
		
		// 실제 이메일 처리 로직
		try {
			
			Thread.sleep(1000);
			
			if(Math.random() >= failRate) { // 메시지 발송 성공(99%)
				log.info("[ EmailChannel ] - Sent Successfully to :{}");
				int randNum = (int)(Math.random() * 900000) + 100000; 
				return ChannelSendResult.success("SMTP-" + randNum, 200);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("[ EmailChannel ] - Fail to Send :{}");
		return ChannelSendResult.fail("SMTP_ERR", "1% 실패 처리" , 500);
		
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.EMAIL;
	}

}
