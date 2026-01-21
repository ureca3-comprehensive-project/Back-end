package org.backend.billingbatch.dto;

import java.math.BigDecimal;

public record TotalFeeCalculatorRequest(BigDecimal usage,
                                        BigDecimal servicedAmount,
                                        BigDecimal unitPrice) {





    public TotalFeeCalculatorRequest(String usage,
                                     String servicedAmount,
                                     String unitPrice
) {

        this(new BigDecimal(usage),
                new BigDecimal(servicedAmount),
                new BigDecimal(unitPrice));
    }
}