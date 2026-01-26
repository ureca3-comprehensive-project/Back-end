package org.backend.billingbatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ContractInfo {
    private Long line_id;
    private Long user_id;
    private Long plan_id;
    private LocalDateTime startDate;

    private BigDecimal base_price;

    private BigDecimal voiceLimit;
    private BigDecimal voiceUnitPrice;

    private BigDecimal dataLimit;
    private BigDecimal dataUnitPrice;

    private BigDecimal rate;
    private BigDecimal discountLimit;

    private BigDecimal voice_usage;
    private BigDecimal data_usage;
    private BigDecimal vas;


}
