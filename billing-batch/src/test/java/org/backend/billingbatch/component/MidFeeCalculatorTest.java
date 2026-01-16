package org.backend.billingbatch.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.backend.billingbatch.dto.MidFeeCalculateRequest;
import org.junit.jupiter.api.Test;



class MidFeeCalculatorTest {

    @Test
    void 중간_저장액이_기댓값과_차이가_0이어야함(){

        MidFeeCalculator midFeeCalculator = new MidFeeCalculator();

        //given
        MidFeeCalculateRequest midFeeCalculateRequest = new MidFeeCalculateRequest("100.1",
                "50",
                "10",
                150);

        //when
        BigDecimal result = midFeeCalculator.midFeeCalculate(midFeeCalculateRequest);
        BigDecimal myResult = new BigDecimal(651);
        //then
        assertEquals(0,result.compareTo(myResult));

    }


    @Test
    void 기본_제공량_이하로_쓰면_기본료만_청구됨(){

        MidFeeCalculator midFeeCalculator = new MidFeeCalculator();

        //given
        MidFeeCalculateRequest midFeeCalculateRequest = new MidFeeCalculateRequest("100.1",
                "1000",
                "10",
                150);

        //when
        BigDecimal result = midFeeCalculator.midFeeCalculate(midFeeCalculateRequest);
        BigDecimal myResult = new BigDecimal("150");
        //then
        assertEquals(0,result.compareTo(myResult));

    }



}