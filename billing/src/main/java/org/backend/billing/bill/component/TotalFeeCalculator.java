package org.backend.billing.bill.component;

import java.math.BigDecimal;
import org.backend.billing.bill.dto.TotalFeeCalculatorRequest;
import org.springframework.stereotype.Component;

@Component
public class TotalFeeCalculator {

    public BigDecimal totalFeeCalculate(TotalFeeCalculatorRequest request){

        

        BigDecimal result = midFeeCalculate(request.usage(),
                request.servicedAmount(),
                request.unitPrice(),
                request.basePrice())
                .add(request.midFee())
                .add(request.vas())
                .subtract(request.discount());

        return result;

    }

    private static BigDecimal midFeeCalculate(BigDecimal usage,
                                              BigDecimal servicedAmount,
                                              BigDecimal unitPrice,
                                              BigDecimal baseFee) {
        return ((usage.subtract(servicedAmount)).multiply(unitPrice)).add(baseFee);
    }


}
