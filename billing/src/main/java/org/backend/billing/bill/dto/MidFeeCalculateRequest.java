package org.backend.billing.bill.dto;

import java.math.BigDecimal;

public record MidFeeCalculateRequest(BigDecimal usage,
                                     BigDecimal servicedAmount,
                                     BigDecimal unitPrice,
                                     BigDecimal basePrice) {

    public MidFeeCalculateRequest(String usage, String servicedAmount, String unitPrice,
                                  Integer basePrice) {
        this(
        new BigDecimal(usage),
        new BigDecimal(servicedAmount),
        new BigDecimal(unitPrice),
        BigDecimal.valueOf(basePrice)
        );
    }
}