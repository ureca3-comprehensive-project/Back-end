package org.backend.billingbatch.dto;

import java.math.BigDecimal;

public record TotalFeeCalculatorRequest(BigDecimal usage,
                                        BigDecimal servicedAmount,
                                        BigDecimal unitPrice,
                                        BigDecimal basePrice,
                                        BigDecimal midFee,
                                        BigDecimal vas,
                                        BigDecimal discount) {


    public TotalFeeCalculatorRequest(String usage,
                                     String servicedAmount,
                                     String unitPrice,
                                     Integer basePrice,
                                     BigDecimal midFee,
                                     BigDecimal vas,
                                     BigDecimal discount) {

        this(new BigDecimal(usage),
                new BigDecimal(servicedAmount),
                new BigDecimal(unitPrice),
                BigDecimal.valueOf(basePrice),
                midFee,
                vas,
                discount);
    }
}