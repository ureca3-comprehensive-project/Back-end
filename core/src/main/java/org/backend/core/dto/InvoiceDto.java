package org.backend.core.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class InvoiceDto {
	
	private Long id;
    private String billingMonth;
    private Long totalAmount;
    private LocalDate dueDate;

    // 회선 기준
    private String phone;
	
	
	
	

}
