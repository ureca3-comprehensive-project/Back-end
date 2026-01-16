package org.backend.core.message.type;

public enum ChannelType {
	
	EMAIL(1,"EMAIL"),
    SMS(2,"SMS"),
    PUSH(3,"PUSH"),
    ETC(4,"ETC");
	
	private final int code;
	private final String description;
	
	ChannelType(int code, String description) {
        this.code = code;
		this.description = description;
    }

}
