package org.backend.billing.message.type;


public enum MessageType {
    EMAIL(1),
    SMS(2),
    PUSH(3),
    ETC(9);

    private final int code;
    MessageType(int code) { this.code = code; }
    public int getCode() { return code; }

    public static MessageType fromCode(int code) {
        for (MessageType t : values()) if (t.code == code) return t;
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
  
    }
}