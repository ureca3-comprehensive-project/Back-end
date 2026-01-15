package org.backend.billing.bill.component;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.backend.billing.bill.dto.MidFeeCalculateRequest;
import org.backend.billing.bill.dto.TotalFeeCalculatorRequest;
import org.junit.jupiter.api.Test;

class TotalFeeCalculatorTest {

    @Test
    void 최종_저장액이_기댓값과_차이가_0이어야함(){

        TotalFeeCalculator totalFeeCalculator = new TotalFeeCalculator();
        MidFeeCalculator midFeeCalculator = new MidFeeCalculator();

        //given
        MidFeeCalculateRequest midFeeCalculateRequest = new MidFeeCalculateRequest("100.1",
                "50",
                "10",
                150);
        BigDecimal midFee = midFeeCalculator.midFeeCalculate(midFeeCalculateRequest);

        //when
        TotalFeeCalculatorRequest totalFeeCalculatorRequest = new TotalFeeCalculatorRequest("100.1",
                "50",
                "10",
                150,
                midFee,
                new BigDecimal("32.7"),
                new BigDecimal("62.6"));

        BigDecimal totalResult = totalFeeCalculator.totalFeeCalculate(totalFeeCalculatorRequest);
        BigDecimal myResult = new BigDecimal("1272.1");
//      //then
        assertEquals(0,totalResult.compareTo(myResult));


    }


}