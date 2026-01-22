package org.backend.message.channel.impl;

import org.backend.core.dto.TemplateDto;
import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.core.util.security.masking.MaskingType;
import org.backend.core.util.security.masking.MaskingUtil;
import org.backend.domain.line.repository.LineRepository;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.template.entity.Template;
import org.backend.message.channel.MessageChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SMSChannel implements MessageChannel {
	
	private final LineRepository lineRepository;

	@Override
	public ChannelSendResult send(MessageAttempt message) {
		
		String cypherPhone = lineRepository.findPhoneByAttemptId(message.getId());
		String plainPhone = CryptoUtil.decrypt(cypherPhone);
		String maskingPhone= MaskingUtil.mask(plainPhone, MaskingType.PHONE);

		
		log.info("[ SMSChannel ] - Sending SMS to :{}, CorrelationId: {}" , maskingPhone ,message.getMessage().getCorrelationId());
		
		// 실제 푸시 알림 처리 로직
		
		// 푸시알림 발송 완료 시
		log.info("[ SMSChannel ] - Sent Successfully to :{}", maskingPhone);		
		int randNum = (int)(Math.random() * 900000) + 100000; 
		return ChannelSendResult.success("SMS-" + randNum, 200);
	}

	@Override
	public boolean supports(ChannelType channelType) {
		return channelType == ChannelType.SMS;
	}

}
