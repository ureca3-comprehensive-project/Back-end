package org.backend.message.common.dto;


import org.backend.domain.message.type.ChannelType;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageSendEvent {

	private Long messageId;
	private String dedupKey;
	private ChannelType ChannelType;
	private String correlationId;
	
	
}
