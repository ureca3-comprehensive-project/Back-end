package org.backend.message.channel;

import org.backend.core.dto.TemplateDto;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.common.dto.ChannelSendResult;

public interface MessageChannel {
	
	ChannelSendResult send(MessageAttempt message);
    boolean supports(ChannelType channelType);
}
