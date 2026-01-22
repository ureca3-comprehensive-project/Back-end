package org.backend.billingbatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// domain 엔티티 사용으로 수정하면서 다른 코드들도 변경 필요
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    private Long lineId;
    private Long billingId;

    @Column(length = 7)
    private String billingMonth;
    private BigDecimal totalAmount; // 최종 납부 금액 (부가세 포함)
    private String status; // CREATED(생성됨), SEND_REQ(발송 요청), PAID(납부 완료)
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 사용 시 리스트 초기화 유지
    private List<InvoiceDetail> details = new ArrayList<>();

    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
}
