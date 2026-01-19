package org.backend.message.channel;

import org.backend.core.message.type.ChannelType;
import org.backend.message.common.dto.MessageSendEvent;

public interface MessageChannel {
	
	void send(MessageSendEvent message);
    boolean supports(ChannelType channelType);

}
