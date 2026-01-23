package org.backend.domain.billing.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.backend.domain.line.entity.Line;
import org.backend.domain.plan.entity.Plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "BillingHistory",
        indexes = {
        		@Index(name = "idx_billing_history_line_id", columnList = "line_id"),
        })
public class BillingHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "billing_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;
	
    @Column(name = "plan_id", nullable = false)
    private Long planId;
	
	@Column(nullable = false, name = "usage")
	private int usage;
	
	@Column(precision = 20, scale = 2 , nullable = false, name = "amount")
	private BigDecimal amount;
	
	@Column(nullable = false, name = "user_at")
	private LocalDateTime userAt;
	
	@Column(nullable = false, name = "billing_month")
	private String billingMonth;
	
	@Column(precision = 20, scale = 2 , nullable = false , name = "benefit_amount")
	private BigDecimal benefitAmount;
	

}
