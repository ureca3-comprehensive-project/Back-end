package org.backend.billingbatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class MicroPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "micropayment_id")
    private Long micropaymentId;

    private Long lineId;

    @Column(length = 7)
    private String payMonth; // "YYYY-MM"

    private BigDecimal payPrice;

    public MicroPayment(Long lineId, String payMonth, BigDecimal payPrice) {
        this.lineId = lineId;
        this.payMonth = payMonth;
        this.payPrice = payPrice;
    }
}
