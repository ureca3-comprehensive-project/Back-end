package org.backend.domain.vas.entity;

import java.time.LocalDateTime;

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
@Table(name = "line_vas_subscription",
        indexes = {
                @Index(name = "idx_line_vas_subscription_line_id", columnList = "line_id"),
                @Index(name = "idx_line_vas_subscription_vas_id", columnList = "vas_id")
        })
public class LineVasSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    @Column(name = "vas_id", nullable = false)
    private Integer vasId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
    
}