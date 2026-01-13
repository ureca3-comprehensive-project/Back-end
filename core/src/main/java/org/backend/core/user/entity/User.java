package org.backend.core.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.backend.core.common.entity.BaseEntity;
import org.backend.core.user.type.UserStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone")
        })
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email; // 암호화는 모듈에서 처리

    @Column(nullable = false, length = 255, unique = true)
    private String phone; // 암호화는 모듈에서 처리

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "date", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    private Double agreement;
    private Double combination;
    private Double benefits;
}