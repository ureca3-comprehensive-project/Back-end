package org.backend.billing.message.dto.response;

import java.util.List;

public record TemplateResponse(
		Long templateId,
	    String name,
	    String content,
	    String channel
) {}