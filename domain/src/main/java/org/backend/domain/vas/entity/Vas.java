package org.backend.domain.vas.entity;

import java.math.BigDecimal;

import org.backend.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vas",
        indexes = {
                @Index(name = "idx_vas_name", columnList = "name")
        })
public class Vas extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vas_id")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "monthly_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal monthlyPrice;
}