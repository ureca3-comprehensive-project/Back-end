package org.backend.message.common.util;

import tools.jackson.databind.ObjectMapper;

public class PayloadJsonUtil {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

    private PayloadJsonUtil() {
    }

    public static String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Payload JSON 변환 실패", e);
        }
    }

}
