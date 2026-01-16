package org.backend.billingbatch.component;

import java.math.BigDecimal;

import org.backend.billingbatch.dto.TotalFeeCalculatorRequest;
import org.springframework.stereotype.Component;

@Component
public class TotalFeeCalculator {

    public BigDecimal totalFeeCalculate(TotalFeeCalculatorRequest request){

        

        BigDecimal result = totalFeeCalculate(request.usage(),
                request.servicedAmount(),
                request.unitPrice(),
                request.basePrice())
                .add(request.midFee())
                .add(request.vas())
                .subtract(request.discount());

        return result;

    }

    private static BigDecimal totalFeeCalculate(BigDecimal usage,
                                              BigDecimal servicedAmount,
                                              BigDecimal unitPrice,
                                              BigDecimal baseFee) {
        return ((usage.subtract(servicedAmount).max(BigDecimal.ZERO)).multiply(unitPrice)).add(baseFee);
    }


}
