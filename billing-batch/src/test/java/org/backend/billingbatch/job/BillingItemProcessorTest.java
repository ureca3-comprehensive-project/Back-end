package org.backend.billingbatch.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.backend.billingbatch.component.DiscountCalculator;
import org.backend.billingbatch.component.TotalFeeCalculator;
import org.backend.billingbatch.dto.BillingResponse;
import org.backend.billingbatch.dto.ContractInfo;
import org.backend.billingbatch.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingItemProcessorTest {

    @Mock
    private TotalFeeCalculator totalFeeCalculator;

    @Mock
    private DiscountCalculator discountCalculator;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillingItemProcessor billingItemProcessor;

    @Test
    void testProcess() throws Exception {
        // Given
        Long lineId = 1L;
        String billingMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));


        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setLine_id(lineId);
        contractInfo.setPlan_id(100L);
        contractInfo.setStartDate(LocalDateTime.now());
        contractInfo.setBase_price(new BigDecimal("50000"));
        contractInfo.setVoiceLimit(new BigDecimal("100"));
        contractInfo.setVoiceUnitPrice(new BigDecimal("10"));
        contractInfo.setDataLimit(new BigDecimal("1000"));
        contractInfo.setDataUnitPrice(new BigDecimal("20"));
        contractInfo.setDiscountRate(new BigDecimal("0.1"));
        contractInfo.setDiscountLimit(new BigDecimal("5000"));

        BigDecimal voiceUsage = new BigDecimal("120");
        BigDecimal dataUsage = new BigDecimal("800");
        BigDecimal vasAmount = new BigDecimal("2000");
        

        when(billRepository.getUsageSum(eq(lineId), eq("VOICE"), eq(billingMonth))).thenReturn(voiceUsage);
        when(billRepository.getUsageSum(eq(lineId), eq("DATA"), eq(billingMonth))).thenReturn(dataUsage);
        when(billRepository.getVasSum(eq(lineId))).thenReturn(vasAmount);


        BigDecimal calculatedVoiceFee = new BigDecimal("200");
        BigDecimal calculatedDataFee = new BigDecimal("0");
        
        when(totalFeeCalculator.totalFeeCalculate(any())).thenReturn(calculatedVoiceFee, calculatedDataFee);

        
        BigDecimal discountedBasePrice = new BigDecimal("45000");
        when(discountCalculator.discountCalculate(any(), any(), any())).thenReturn(discountedBasePrice);

        // When
        BillingResponse response = billingItemProcessor.process(contractInfo);

        // Then
        assertNotNull(response);
        assertEquals(lineId, response.getLine_id());
        

        BigDecimal expectedTotal = calculatedVoiceFee.add(calculatedDataFee).add(vasAmount).add(discountedBasePrice);
        assertEquals(expectedTotal, response.getAmount());
        
        BigDecimal expectedUsage = voiceUsage.add(dataUsage);
        assertEquals(expectedUsage, response.getUsage());
        
        assertEquals(billingMonth, response.getBillingMonth());
    }
}
