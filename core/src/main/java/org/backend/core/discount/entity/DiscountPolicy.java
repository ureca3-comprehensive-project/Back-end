package org.backend.core.discount.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;
import org.backend.core.user.type.DiscountCategory;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "discount_policy")
public class DiscountPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountCategory category;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate; // 할인율

    @Column(name = "discount_limit", nullable = false, precision = 20, scale = 2)
    private BigDecimal discountLimit; // 최대 할인 금액
}