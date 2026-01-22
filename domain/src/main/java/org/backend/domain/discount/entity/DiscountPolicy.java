package org.backend.domain.discount.entity;

import java.math.BigDecimal;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.user.type.DiscountCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "discount_policy")
public class DiscountPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_ids")
    private Integer id;

    @Column(nullable = false, length = 100, name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, name = "category")
    private DiscountCategory category;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate; // 할인율

    @Column(name = "discount_limit", nullable = false, precision = 20, scale = 2)
    private BigDecimal discountLimit; // 최대 할인 금액
}