package org.backend.message.channel.impl;

import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.core.util.security.masking.MaskingType;
import org.backend.core.util.security.masking.MaskingUtil;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.user.repository.UserRepository;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailChannel implements MessageChannel {
	
	private final double failRate = 0.01;
	private UserRepository userRepository;

	@Override
	public ChannelSendResult send(MessageAttempt message) {
		
		String cypherEmail = userRepository.findEmailByAttemptId(message.getId());
		String plainEmail = CryptoUtil.decrypt(cypherEmail);
		String maskingEmail= MaskingUtil.mask(plainEmail, MaskingType.EMAIL);

		
		log.info("[ EmailChannel ] - Sending EMAIL to :{}, CorrelationId: {}" , maskingEmail,message.getMessage().getCorrelationId());
		
		// 실제 이메일 처리 로직
		try {
			
			// 1초 발송 딜레이
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
