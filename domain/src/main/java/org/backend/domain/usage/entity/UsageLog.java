package org.backend.domain.usage.entity;

import java.math.BigDecimal;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.plan.type.ItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "usage_log",
        indexes = {
                @Index(name = "idx_usage_log_line_id", columnList = "line_id"),
                @Index(name = "idx_usage_log_month", columnList = "log_month")
        })
public class UsageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_log_id")
    private Long id;

    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "used_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal usedAmount;

    @Column(name = "log_month", nullable = false)
    private String logMonth;
}