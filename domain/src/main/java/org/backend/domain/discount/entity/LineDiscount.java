package org.backend.domain.discount.entity;

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
@Table(name = "LineDiscount",
        indexes = {
//                @Index(name = "idx_line_discount_user_id", columnList = "user_id"),
                @Index(name = "idx_line_discount_discount_id", columnList = "discount_id")
        })
public class LineDiscount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_discount_id")
    private Integer id;

    @Column(name = "line_id", nullable = false)
    private Long lineId;

    @Column(name = "discount_id")
    private Integer policyId;


}