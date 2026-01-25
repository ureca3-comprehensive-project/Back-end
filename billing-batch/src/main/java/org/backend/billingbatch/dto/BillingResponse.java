package org.backend.billingbatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BillingResponse {

    private Long line_id;
    private Long plan_id;
    private BigDecimal usage;
    private BigDecimal amount;
    private LocalDateTime userAt;
    private String billingMonth;
    private BigDecimal benefitAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}


