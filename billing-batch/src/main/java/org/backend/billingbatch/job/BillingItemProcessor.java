package org.backend.billingbatch.job;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.billingbatch.component.DiscountCalculator;
import org.backend.billingbatch.component.TotalFeeCalculator;
import org.backend.billingbatch.dto.BillingResponse;
import org.backend.billingbatch.dto.ContractInfo;
import org.backend.billingbatch.dto.TotalFeeCalculatorRequest;
import org.backend.billingbatch.repository.BillRepository;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor

public class BillingItemProcessor implements ItemProcessor<ContractInfo, BillingResponse> {


    private final TotalFeeCalculator totalFeeCalculator;
    private final DiscountCalculator discountCalculator;
    private final BillRepository billRepository;


    @Override
    public BillingResponse process(ContractInfo item) throws Exception {

        LocalDate now = LocalDate.now();
        String billingMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String billingPreMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        BigDecimal voiceUsage = item.getVoice_usage();
        BigDecimal dataUsage = item.getData_usage();
        BigDecimal vasAmount = item.getVas();

        TotalFeeCalculatorRequest voice_request = new TotalFeeCalculatorRequest(voiceUsage, item.getVoiceLimit(),
                item.getVoiceUnitPrice());
        TotalFeeCalculatorRequest data_request = new TotalFeeCalculatorRequest(dataUsage, item.getDataLimit(),
                item.getDataUnitPrice());

        BigDecimal voiceAmount = totalFeeCalculator.totalFeeCalculate(voice_request);
        BigDecimal dataAmount = totalFeeCalculator.totalFeeCalculate(data_request);

        BigDecimal discount = discountCalculator.discountCalculate(item.getDiscountRate(), item.getBase_price(),
                item.getDiscountLimit());

        BigDecimal totalSum = voiceAmount.add(dataAmount).add(vasAmount).add(discount);
        log.info("id = {} ",item.getLine_id());

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
