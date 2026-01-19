package org.backend.message.channel;

import org.backend.core.message.entity.Message;
import org.backend.core.message.type.ChannelType;

public interface MessageChannel {
	
	boolean send(Message message);
    boolean supports(ChannelType channelType);

}
