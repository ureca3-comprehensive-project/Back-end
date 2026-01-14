package org.backend.core.line.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;
import org.backend.core.line.type.LineStatus;
import org.backend.core.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "line",
        indexes = {
                @Index(name = "idx_line_user_id", columnList = "user_id"),
                @Index(name = "idx_line_plan_id", columnList = "plan_id"),
                @Index(name = "idx_line_phone_num", columnList = "phone_num")
        })
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LineStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "phone_num", nullable = false, unique = true)
    private String phoneNum; // 암호화는 모듈에서 처리
}