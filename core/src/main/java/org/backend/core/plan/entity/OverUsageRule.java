package org.backend.core.plan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "over_usage_rule",
        indexes = {
                @Index(name = "idx_over_usage_rule_item_id", columnList = "item_id")
        })
public class OverUsageRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private PlanItem planItem;

    @Column(name = "charge_unit", nullable = false)
    private String chargeUnit;

    @Column(name = "additional_price", nullable = false)
    private String additionalPrice;
}