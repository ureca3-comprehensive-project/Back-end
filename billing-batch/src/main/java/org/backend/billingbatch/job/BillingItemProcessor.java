package org.backend.billingbatch.job;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.component.DiscountCalculator;
import org.backend.billingbatch.component.TotalFeeCalculator;
import org.backend.billingbatch.dto.BillingResponse;
import org.backend.billingbatch.dto.ContractInfo;
import org.backend.billingbatch.dto.TotalFeeCalculatorRequest;
import org.backend.billingbatch.repository.BillRepository;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class BillingItemProcessor implements ItemProcessor<ContractInfo, BillingResponse> {


    private final TotalFeeCalculator totalFeeCalculator;
    private final DiscountCalculator discountCalculator;
    private final BillRepository billRepository;

    @Override
    public BillingResponse process(ContractInfo item) throws Exception {

        LocalDate now = LocalDate.now();
        String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

        BigDecimal voiceUsage = billRepository.getUsageSum(item.getLine_id(), "VOICE", billingMonth);
        BigDecimal dataUsage = billRepository.getUsageSum(item.getLine_id(), "DATA", billingMonth);
        BigDecimal vasAmount = billRepository.getVasSum(item.getLine_id());

        TotalFeeCalculatorRequest voice_request = new TotalFeeCalculatorRequest(voiceUsage, item.getVoiceLimit(),
                item.getVoiceUnitPrice());
        TotalFeeCalculatorRequest data_request = new TotalFeeCalculatorRequest(dataUsage, item.getDataLimit(),
                item.getDataUnitPrice());

        BigDecimal voiceAmount = totalFeeCalculator.totalFeeCalculate(voice_request);
        BigDecimal dataAmount = totalFeeCalculator.totalFeeCalculate(data_request);

        BigDecimal discount = discountCalculator.discountCalculate(item.getDiscountRate(), item.getBase_price(),
                item.getDiscountLimit());

        BigDecimal totalSum = voiceAmount.add(dataAmount).add(vasAmount).add(discount);

        return new BillingResponse(
                item.getLine_id(),
                item.getPlan_id(),
                voiceUsage.add(dataUsage),
                totalSum,
                item.getStartDate(),
                billingMonth,
                discount
        );


    }
}
