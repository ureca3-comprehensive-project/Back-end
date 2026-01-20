package org.backend.billingbatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceResponse {
    private Long invoiceId;
    private Long lineId;
    private String billingMonth;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime dueDate;
}
