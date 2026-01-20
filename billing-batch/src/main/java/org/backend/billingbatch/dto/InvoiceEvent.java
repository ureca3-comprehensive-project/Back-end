package org.backend.billingbatch.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceEvent {
    private Long invoiceId;
    private Long lineId;    // userId가 없기에
    private String billingMonth;
    private Long totalAmount;
    private String messageType; // "INVOICE_CREATED"
}
