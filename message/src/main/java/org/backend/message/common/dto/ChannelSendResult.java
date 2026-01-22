package org.backend.message.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelSendResult {
	
	private final boolean success;
    private final String providerMessageId;
    private final Integer httpStatus;
    private final String failCode;
    private final String failReason;

    // static factory methods
    public static ChannelSendResult success(String providerMessageId, int httpStatus) {
        return new ChannelSendResult(true, providerMessageId, httpStatus, null, null);
    }

    public static ChannelSendResult fail(String code, String reason, Integer httpStatus) {
        return new ChannelSendResult(false, null, httpStatus, code, reason);
    }

}
