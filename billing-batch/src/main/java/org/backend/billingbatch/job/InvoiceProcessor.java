package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceDto;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.entity.InvoiceDetail;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.Line;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 금액 계산 로직
@Component
@RequiredArgsConstructor
//public class InvoiceProcessor implements ItemProcessor<BillingHistory, Invoice> {
public class InvoiceProcessor implements ItemProcessor<BillingHistory, InvoiceDto> { // invoiceDto 사용 버전
    private final jakarta.persistence.EntityManager entityManager;

//    @Override
//    public Invoice process(BillingHistory history) throws Exception {
//
//        // SELECT 쿼리가 나가지 않으면서 연관관계를 맺기
//        Line lineProxy = entityManager.getReference(Line.class, history.getLine().getId());
//
//        // 청구서(Invoice) 엔티티 생성
//        Invoice invoice = Invoice.builder()
////                .line(lineProxy)
//                .line(Line.builder().id(history.getLine().getId()).build())
//                .billingHistory(history)
//                .billingMonth(history.getBillingMonth())
//                .totalAmount(history.getAmount()) // 계산된 버전이면 바로 넣으면 되서 속도 더 빨라짐
//                .status(InvoiceStatus.CREATED) // 생성됨 상태
//                // 납부 기한 설정 (생성일로부터 3일 뒤)
//                .dueDate(LocalDateTime.now().plusDays(3))
////                .createdAt(LocalDateTime.now())
//                .build();
//
//        // 청구서 상세
//        InvoiceDetail telecomDetail = InvoiceDetail.builder()
//                .invoice(invoice) // 연관관계 설정
//                .billingType("TELECOM_FEE")
//                .amount(history.getAmount())
//                .status("SUCCESS")
//                .build();
//
//        invoice.addDetail(telecomDetail);
//
//        return invoice;
//    }

    // invoiceJobTest 수정 필요
    @Override
    public InvoiceDto process(BillingHistory history) throws Exception {
//        Map<String, Object> map = new HashMap<>();
//
//        map.put("lineId", history.getLine().getId());
//        map.put("billingId", history.getId());
//        map.put("billingMonth", history.getBillingMonth());
//        map.put("totalAmount", history.getAmount());
//        map.put("status", "CREATED");
//        map.put("dueDate", LocalDateTime.now().plusDays(3));
//        map.put("createdAt", LocalDateTime.now());
//
//        return map;

        return new InvoiceDto(
                java.util.UUID.randomUUID().toString(), // 상세 select 없이 넣기 위함
                java.util.UUID.randomUUID().toString(),
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
