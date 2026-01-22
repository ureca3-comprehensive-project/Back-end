package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.entity.InvoiceDetail;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.Line;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 금액 계산 로직
@Component
@RequiredArgsConstructor
public class InvoiceProcessor implements ItemProcessor<BillingHistory, Invoice> {
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    public Invoice process(BillingHistory history) throws Exception {

        // SELECT 쿼리가 나가지 않으면서 연관관계를 맺기
        Line lineProxy = entityManager.getReference(Line.class, history.getLine().getId());

        // 청구서(Invoice) 엔티티 생성
        Invoice invoice = Invoice.builder()
//                .line(lineProxy)
                .line(Line.builder().id(history.getLine().getId()).build())
                .billingHistory(history)
                .billingMonth(history.getBillingMonth())
                .totalAmount(history.getAmount()) // 계산된 버전이면 바로 넣으면 되서 속도 더 빨라짐
                .status(InvoiceStatus.CREATED) // 생성됨 상태
                // 납부 기한 설정 (생성일로부터 3일 뒤)
                .dueDate(LocalDateTime.now().plusDays(3))
//                .createdAt(LocalDateTime.now())
                .build();

        // 청구서 상세
        InvoiceDetail telecomDetail = InvoiceDetail.builder()
                .invoice(invoice) // 연관관계 설정
                .billingType("TELECOM_FEE")
                .amount(history.getAmount())
                .status("SUCCESS")
                .build();

        invoice.addDetail(telecomDetail);

        return invoice;
    }
}
