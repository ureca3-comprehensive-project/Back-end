package org.backend.billingbatch.component;

import java.math.BigDecimal;

import org.backend.billingbatch.dto.MidFeeCalculateRequest;
import org.springframework.stereotype.Component;

@Component
public class MidFeeCalculator {

    public BigDecimal midFeeCalculate(MidFeeCalculateRequest request){



        BigDecimal result = midFeeCalculate(request.usage(),request.servicedAmount(),request.unitPrice(),request.basePrice());
        return result;



    }

    private static BigDecimal midFeeCalculate(BigDecimal usage,
                                              BigDecimal servicedAmount,
                                              BigDecimal unitPrice,
                                              BigDecimal baseFee) {
        return ((usage.subtract(servicedAmount)).max(BigDecimal.ZERO)).multiply(unitPrice).add(baseFee);
    }


}
