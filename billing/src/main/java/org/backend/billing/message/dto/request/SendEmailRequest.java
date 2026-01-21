package org.backend.billing.message.dto.request;

public record SendEmailRequest(
        String email,
        String subject,
        String content,
        String clientRequestId
) {}