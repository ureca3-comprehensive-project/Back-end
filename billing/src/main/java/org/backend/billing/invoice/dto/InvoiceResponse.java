package org.backend.billing.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.type.InvoiceStatus;

@Getter
@Builder
public class InvoiceResponse {
    private Long invoiceId;
    private Long lineId;
    private Long billingId;
    private String billingMonth;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private LocalDateTime dueDate;
    private List<InvoiceDetailResponse> details;

    public static InvoiceResponse fromEntity(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getId())
                // Lazy Loading을 고려하여 연관 관계 ID 추출 시 null 체크
                .lineId(invoice.getLine() != null ? invoice.getLine().getId() : null)
                .billingId(invoice.getBillingHistory() != null ? invoice.getBillingHistory().getId() : null)
                .billingMonth(invoice.getBillingMonth())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                // 상세 내역
                .details(invoice.getDetails() != null ?
                        invoice.getDetails().stream()
                                .map(InvoiceDetailResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
