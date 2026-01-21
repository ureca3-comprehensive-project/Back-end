package org.backend.domain.message.type;

public enum ChannelType {
	
	EMAIL,
    SMS,
    PUSH;

    public ChannelType next() {
        return switch (this) {
            case EMAIL -> SMS;   // 이메일 실패 → SMS
            case SMS   -> PUSH;  // SMS 실패 → 종료
            case PUSH  -> null;  // PUSH 실패 → 종료
        };
    }

}
