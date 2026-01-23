package org.backend.domain.microPayment.entity;

import java.math.BigDecimal;

import org.backend.domain.plan.entity.Plan;
import org.backend.domain.plan.type.ItemType;
import org.backend.domain.usage.entity.UsageLog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "micropayment",
		indexes = {
		        @Index(name = "idx_micropayment_pay_month", columnList = "pay_month")
		})
public class MicroPayment {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "micropayment_id")
    private Integer id;

    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    @Column(name = "pay_month", nullable = false)
    private String payMonth;

    @Column(name = "pay_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal payPrice;

}
