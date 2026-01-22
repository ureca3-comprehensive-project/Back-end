package org.backend.message.channel.impl;

import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.core.util.security.masking.MaskingType;
import org.backend.core.util.security.masking.MaskingUtil;
import org.backend.domain.line.repository.LineRepository;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushChannel implements MessageChannel {
	
	private final LineRepository lineRepository;
	
	@Override
	public ChannelSendResult send(MessageAttempt message) {
		
		String cypherPhone = lineRepository.findPhoneByAttemptId(message.getId());
		String plainPhone = CryptoUtil.decrypt(cypherPhone);
		String maskingPhone= MaskingUtil.mask(plainPhone, MaskingType.PHONE);

		
		log.info("[ PushChannel ] - Sending Push to :{}, CorrelationId: {}", maskingPhone, message.getMessage().getCorrelationId());
		
		// 실제 푸시 알림 처리 로직
		
		int randNum = (int)(Math.random() * 900000) + 100000; 
		
		// 푸시알림 발송 완료 시
		log.info("[ PushChannel ] - Sent Successfully to :{}");
		
		return ChannelSendResult.success("PUSH-" + randNum, 200);
		
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.PUSH;
	}

}
