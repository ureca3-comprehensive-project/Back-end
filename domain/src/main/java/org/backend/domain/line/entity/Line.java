package org.backend.domain.line.entity;

import java.time.LocalDateTime;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.line.type.LineStatus;
import org.backend.domain.user.entity.BanTime;
import org.backend.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Line",
        indexes = {
                @Index(name = "idx_line_user_id", columnList = "user_id"),
                @Index(name = "idx_line_plan_id", columnList = "plan_id"),
                @Index(name = "idx_line_due_date_id", columnList = "due_date_id")
        })
public class Line {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "due_date_id", nullable = false)
    private DueDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LineStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone; // 암호화는 모듈에서 처리
}