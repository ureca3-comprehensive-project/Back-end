package org.backend.billing.message.dto.response;

public record DndTimeResponse(
        String startTime, // "22:00"
        String endTime,   // "08:00"
        String updatedAt
) {}