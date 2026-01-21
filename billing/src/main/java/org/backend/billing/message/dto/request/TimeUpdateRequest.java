package org.backend.billing.message.dto.request;

public record TimeUpdateRequest(
        String startTime, // "22:00"
        String endTime,   // "08:00"
        Boolean enabled //  추가(옵션)
) {}