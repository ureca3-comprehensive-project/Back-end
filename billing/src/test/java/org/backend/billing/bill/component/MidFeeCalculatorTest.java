package org.backend.billing.bill.component;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;



class MidFeeCalculatorTest {

    @Test
    void 중간_저장액이_기댓값과_차이가_0이어야함(){

        MidFeeCalculator midFeeCalculator = new MidFeeCalculator();

        //given
        String usageNow = "100.1";
        String baseUsage = "50";
        String unitPrice = "10";
        Integer basePrice = 150;
        //when
        BigDecimal result = midFeeCalculator.midFeeCalculate(usageNow,baseUsage,unitPrice,basePrice);
        BigDecimal myResult = new BigDecimal(651);
        //then
        assertEquals(0,result.compareTo(myResult));

    }



}