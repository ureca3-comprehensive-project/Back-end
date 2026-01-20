package org.backend.billingbatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Long billingId;

    private Long lineId;
    private Long planId;

    // 사용량 (초/패킷 등)
    private Integer usage;

    // 기본 계산된 금액 (할인 적용 후라고 가정)
    private BigDecimal amount;

    private LocalDateTime userAt;

    @Column(length = 7)
    private String billingMonth; // "YYYY-MM"

    private BigDecimal benefitAmount;

    @Transient // JPA가 관리하지 않도록 설정
    private BigDecimal microPaymentSum = BigDecimal.ZERO;

    // 테스트용 생성자
    public BillingHistory(Long lineId, BigDecimal amount, String billingMonth) {
        this.lineId = lineId;
        this.amount = amount;
        this.billingMonth = billingMonth;
    }
}
