package org.backend.billingbatch.job.invoice;

import java.time.LocalDateTime;

import org.backend.billingbatch.dto.InvoiceDto;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.IdGenerator;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// 금액 계산 로직
@Component
@RequiredArgsConstructor
public class InvoiceProcessor implements ItemProcessor<BillingHistory, InvoiceDto> { // invoiceDto 사용 버전
    private final IdGenerator idGenerator;

    // invoiceJobTest 수정 필요
    @Override
    public InvoiceDto process(BillingHistory history) throws Exception {
        Long invoiceId = idGenerator.nextId();
        Long detailId = idGenerator.nextId();

        return new InvoiceDto(
                invoiceId,
                detailId,
                history.getLine().getId(),
                history.getId(),
                history.getBillingMonth(),
                history.getAmount(),
                "CREATED",
                LocalDateTime.now().plusDays(3),
                "TELECOM_FEE",
                history.getAmount(),
                "BASIC"
        );
    }
}
