package org.backend.billing.message.dto.request;

import java.util.Map;

public record TemplatePreviewRequest(
        Long templateId,
        Map<String, String> variables
) {}