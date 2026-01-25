package org.backend.billingbatch.component;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DiscountCalculator {

    public BigDecimal discountCalculate(BigDecimal rate, BigDecimal basePrice, BigDecimal discountLimit){
        if(rate == null){
            rate = BigDecimal.ZERO;
        }
        if(discountLimit == null){
            discountLimit = BigDecimal.ZERO;
        }

        BigDecimal result = basePrice.subtract(rate.multiply(basePrice));

        if ((discountLimit.compareTo(rate.multiply(basePrice)) <= 0) & (basePrice.compareTo(discountLimit) >= 0)){
            return basePrice.subtract(discountLimit);
        }

        if(result.compareTo(BigDecimal.ZERO) < 0){
            return BigDecimal.ZERO;
        }
        return result;

    }
}
