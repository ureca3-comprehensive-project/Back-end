package org.backend.core.discount.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_discount",
        indexes = {
                @Index(name = "idx_user_discount_user_id", columnList = "user_id"),
                @Index(name = "idx_user_discount_discount_id", columnList = "discount_id")
        })
public class UserDiscount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "discount_id")
    private Integer discountId;
}