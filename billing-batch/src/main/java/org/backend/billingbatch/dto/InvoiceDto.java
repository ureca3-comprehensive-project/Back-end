package org.backend.billingbatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceDto (
    Long invoiceId,    // Invoice PK
    Long detailId,
    Long lineId,
    Long billingId,
    String billingMonth,
    BigDecimal totalAmount,
    String status,
    LocalDateTime dueDate,
    // 상세 내역 정보
    String billingType,
    BigDecimal detailAmount,
    String detailStatus
) {}
