package org.backend.billing.message.dto.request;

import java.time.LocalDateTime;
import java.util.Map;

public record MessageSendRequest(
        Long userId,
        String channel,       // EMAIL / SMS / PUSH
        String destination,   // email/phone/pushToken 등
        Long templateId,
        Map<String, String> variables,
        LocalDateTime scheduledAt // null이면 즉시
) {}