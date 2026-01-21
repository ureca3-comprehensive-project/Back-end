package org.backend.billing.message.dto.request;

import java.util.List;

public record TemplateUpdateRequest(
        Long templateId,
        String name,
        String subjectTemplate,
        String bodyTemplate,
        List<String> allowedVariables
) {}