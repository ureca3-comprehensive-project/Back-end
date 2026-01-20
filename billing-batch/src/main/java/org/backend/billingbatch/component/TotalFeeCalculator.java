package org.backend.billingbatch.component;

import java.math.BigDecimal;

import org.backend.billingbatch.dto.TotalFeeCalculatorRequest;
import org.springframework.stereotype.Component;

@Component
public class TotalFeeCalculator {

    public BigDecimal totalFeeCalculate(TotalFeeCalculatorRequest request){

        BigDecimal result = FeeCalculate(
                request.usage(),
                request.servicedAmount(),
                request.unitPrice()
        );
//
        return result;

    }

    private static BigDecimal FeeCalculate(BigDecimal usage,
                                              BigDecimal servicedAmount,
                                              BigDecimal unitPrice){



        if (servicedAmount == null) servicedAmount = BigDecimal.ZERO;
        if (usage == null) usage = BigDecimal.ZERO;
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;


        return ((usage.subtract(servicedAmount).max(BigDecimal.ZERO)).multiply(unitPrice));
    }

    }



