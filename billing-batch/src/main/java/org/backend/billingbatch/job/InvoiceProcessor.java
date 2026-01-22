package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.Line;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 금액 계산 로직
@Component
@RequiredArgsConstructor
public class InvoiceProcessor implements ItemProcessor<org.backend.billingbatch.entity.BillingHistory, Invoice> {
    private final jakarta.persistence.EntityManager entityManager;
    // 부가세율 10%
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(1.1);

    @Override
    public Invoice process(org.backend.billingbatch.entity.BillingHistory history) throws Exception {
        // 통신 요금 (BillingHistory에서 가져옴)
        BigDecimal telecomAmount = history.getAmount();

        // 소액 결제 요금 합산 (DB 조회)
        BigDecimal microPaymentSum = history.getMicroPaymentSum();

        // 최종 청구 금액 (공급가액 * 1.1) -> 소수점 처리는 정리 필요
        BigDecimal finalAmount = telecomAmount.add(microPaymentSum).multiply(VAT_RATE).setScale(0, java.math.RoundingMode.FLOOR);

        // SELECT 쿼리가 나가지 않으면서 연관관계를 맺기
        Line lineProxy = entityManager.getReference(Line.class, history.getLineId());
        BillingHistory billingHistoryProxy = entityManager.getReference(BillingHistory.class, history.getBillingId());
        // 청구서(Invoice) 엔티티 생성
        Invoice invoice = Invoice.builder()
                .line(lineProxy)
                .billingHistory(billingHistoryProxy)
                .billingMonth(history.getBillingMonth())
                .totalAmount(finalAmount) // 계산된 버전이면 바로 넣으면 되서 속도 더 빨라짐
                .status(InvoiceStatus.CREATED) // 생성됨 상태
                // 납부 기한 설정 (생성일로부터 3일 뒤)
                .dueDate(LocalDateTime.now().plusDays(3))
//                .createdAt(LocalDateTime.now())
                .build();

        // 청구서 상세
//        InvoiceDetail telecomDetail = InvoiceDetail.builder()
//                .invoice(invoice) // 연관관계 설정
//                .billingType("TELECOM_FEE")
//                .amount(history.getAmount())
//                .status("SUCCESS")
//                .build();
//
//        invoice.addDetail(telecomDetail);

        return invoice;
    }
}
