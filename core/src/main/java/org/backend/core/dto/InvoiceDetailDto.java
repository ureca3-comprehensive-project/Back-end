package org.backend.core.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceDetailDto {
	
	private String billingType;
    private Long amount;

    /**
     * true  = 과금(+)
     * false = 할인(-)
     */
    private boolean positive;

}
