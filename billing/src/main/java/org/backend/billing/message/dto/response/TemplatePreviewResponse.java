package org.backend.billing.message.dto.response;

public record TemplatePreviewResponse(
	    Long templateId,
	    int version,
	    String subject,
	    String body
	) {}