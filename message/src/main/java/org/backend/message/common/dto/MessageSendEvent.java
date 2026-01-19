package org.backend.message.common.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageSendEvent {

	private int messageId;
	private String dedeupKey;
	private String ChannelType;
	private String correlationId;
	
	
}
