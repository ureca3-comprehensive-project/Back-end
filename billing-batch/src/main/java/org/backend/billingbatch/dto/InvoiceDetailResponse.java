package org.backend.billingbatch.dto;

import lombok.Builder;
import lombok.Getter;
import org.backend.billingbatch.entity.InvoiceDetail;

import java.math.BigDecimal;

@Getter
@Builder
public class InvoiceDetailResponse {
    private Long invoiceDetailId;
    private String billingType;
    private BigDecimal amount;
    private String status;

    public static InvoiceDetailResponse from(InvoiceDetail detail) {
        return InvoiceDetailResponse.builder()
                .invoiceDetailId(detail.getInvoiceDetailId())
                .billingType(detail.getBillingType())
                .amount(detail.getAmount())
                .status(detail.getStatus())
                .build();
    }
}
