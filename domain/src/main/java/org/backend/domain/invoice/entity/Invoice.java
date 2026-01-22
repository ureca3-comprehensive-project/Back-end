package org.backend.domain.invoice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.Line;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
@Table(name = "invoice",
        indexes = {
        		@Index(name = "idx_invoice_line_id", columnList = "line_id"),
        		@Index(name = "idx_invoice_billing_month", columnList = "billing_month"),
        })
public class Invoice extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "billing_id", nullable = false)
	private BillingHistory billing;

    @Column(length = 7, name = "billing_month")
    private String billingMonth;
    
    @Column(length = 7, name = "total_amount")
    private BigDecimal totalAmount; // 최종 납부 금액 (부가세 포함)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private InvoiceStatus status; // CREATED(생성됨), SEND_REQ(발송 요청), PAID(납부 완료)
    
    private LocalDateTime dueDate;

    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 사용 시 리스트 초기화 유지
    private List<InvoiceDetail> details = new ArrayList<>();

    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
	

}
