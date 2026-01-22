package org.backend.billingbatch.dto;

import lombok.*;
import org.backend.domain.invoice.entity.InvoiceDetail;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailResponse {
    private Long invoiceDetailId;
    private String billingType;
    private BigDecimal amount;

    public static InvoiceDetailResponse from(InvoiceDetail detail) {
        return InvoiceDetailResponse.builder()
                .invoiceDetailId(detail.getId())
                .billingType(detail.getBillingType())
                .amount(detail.getAmount())
                .build();
    }
}
