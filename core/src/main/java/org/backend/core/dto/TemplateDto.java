package org.backend.core.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateDto {
	
	private Integer id;
    private String type;
    private String title;
    private String body;

}
