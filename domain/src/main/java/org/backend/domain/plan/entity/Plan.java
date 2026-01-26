package org.backend.domain.plan.entity;

import java.math.BigDecimal;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.plan.type.NetworkType;

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
@Table(name = "Plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "base_price", nullable = false , precision = 20, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_type", nullable = false, length = 10)
    private NetworkType networkType;
}