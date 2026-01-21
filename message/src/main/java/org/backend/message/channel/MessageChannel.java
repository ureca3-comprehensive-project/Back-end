package org.backend.message.channel;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.common.dto.ChannelSendResult;

public interface MessageChannel {
	
	ChannelSendResult send(Message message);
    boolean supports(ChannelType channelType);

}
