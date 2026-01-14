package org.backend.billing.bill.component;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TotalFeeCalculator {

    public BigDecimal totalFeeCalculate(String amountUsage,
                                        String baseAmount,
                                        String additionalPrice,
                                        Integer basePrice,
                                        BigDecimal midFee,
                                        BigDecimal vas,
                                        BigDecimal discount){

        BigDecimal usage = new BigDecimal(amountUsage);
        BigDecimal servicedAmount = new BigDecimal(baseAmount);
        BigDecimal unitPrice = new BigDecimal(additionalPrice);
        BigDecimal baseFee = new BigDecimal(basePrice);

        BigDecimal result = ((usage.subtract(servicedAmount)).multiply(unitPrice)).add(baseFee).add(midFee).add(vas).subtract(discount);

        return result;

    }







    }
