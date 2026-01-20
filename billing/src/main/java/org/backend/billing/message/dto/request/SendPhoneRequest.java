package org.backend.billing.message.dto.request;

public record SendPhoneRequest(
        String phone,
        String content,
        String clientRequestId // 중복 방지용(선택)
) {}
