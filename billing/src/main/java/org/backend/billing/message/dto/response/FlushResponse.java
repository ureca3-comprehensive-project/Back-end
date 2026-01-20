package org.backend.billing.message.dto.response;

public record FlushResponse(
        int flushed,
        int queuedLeft
) {}