package org.backend.core.usage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;
import org.backend.core.plan.type.ItemType;

@Getter
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
    private Long id;

    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "used_amount", nullable = false)
    private String usedAmount;

    @Column(name = "log_month", nullable = false)
    private String logMonth;
}