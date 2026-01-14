package org.backend.billing.bill.component;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MidFeeCalculator {

    public BigDecimal midFeeCalculate(String amountUsage, String baseAmount,String additionalPrice,Integer basePrice ){
        BigDecimal usage = new BigDecimal(amountUsage);
        BigDecimal servicedAmount = new BigDecimal(baseAmount);
        BigDecimal unitPrice = new BigDecimal(additionalPrice);
        BigDecimal baseFee = new BigDecimal(basePrice);
        BigDecimal result = ((usage.subtract(servicedAmount)).multiply(unitPrice)).add(baseFee);
        return result;



    }


}
