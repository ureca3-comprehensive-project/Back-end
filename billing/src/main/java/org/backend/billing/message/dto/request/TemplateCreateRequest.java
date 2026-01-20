package org.backend.billing.message.dto.request;

import java.util.List;

public record TemplateCreateRequest(
        String name,
        String channel,          // EMAIL / SMS / PUSH
        String subjectTemplate,  // EMAIL에서만 사용 가능
        String bodyTemplate,
        List<String> allowedVariables
) {}