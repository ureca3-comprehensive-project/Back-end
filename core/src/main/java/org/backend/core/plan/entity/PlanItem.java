package org.backend.core.plan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;
import org.backend.core.plan.type.ItemType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "plan_item",
        indexes = {
                @Index(name = "idx_plan_item_plan_id", columnList = "plan_id"),
                @Index(name = "idx_plan_item_type", columnList = "item_type")
        })
public class PlanItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "limit_amount", nullable = false)
    private String limitAmount;

    @Column(name = "unit_type", nullable = false)
    private String unitType;
}