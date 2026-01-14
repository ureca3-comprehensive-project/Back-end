package org.backend.billing.bill.component;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.backend.billing.bill.dto.MidFeeCalculateRequest;
import org.junit.jupiter.api.Test;

class TotalFeeCalculatorTest {

    @Test
    void 최종_저장액이_기댓값과_차이가_0이어야함(){

        TotalFeeCalculator totalFeeCalculator = new TotalFeeCalculator();
        MidFeeCalculator midFeeCalculator = new MidFeeCalculator();

        //given
        String usageNow = "100.1";
        String baseUsage = "50";
        String unitPrice = "10";
        Integer basePrice = 150;
        BigDecimal vas = new BigDecimal("32.7");
        BigDecimal discount = new BigDecimal("62.6");

        //when
        BigDecimal midResult = midFeeCalculator.midFeeCalculate(
                new MidFeeCalculateRequest(usageNow, baseUsage, unitPrice, basePrice));
        BigDecimal totalResult = totalFeeCalculator.totalFeeCalculate(usageNow,baseUsage,unitPrice,basePrice,midResult,vas,discount);
        BigDecimal myResult = new BigDecimal("1272.1");
//      //then
        assertEquals(0,totalResult.compareTo(myResult));


    }


}