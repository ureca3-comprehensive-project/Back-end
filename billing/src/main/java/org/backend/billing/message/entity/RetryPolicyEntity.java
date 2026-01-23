package org.backend.billing.message.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "retry_policy")
public class RetryPolicyEntity {

    @Id
    private Long id = 1L; // ✅ 단일 설정 row로 고정

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 3;

    @Column(name = "base_delay_millis", nullable = false)
    private long baseDelayMillis = 300;

    @Column(name = "backoff_multiplier", nullable = false)
    private double backoffMultiplier = 2.0;

    @Column(name = "timeout_millis", nullable = false)
    private long timeoutMillis = 3000;

    @Column(name = "email_fail_rate", nullable = false)
    private double emailFailRate = 0.01;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RetryPolicyEntity() {}

    public Long getId() { return id; }
    public int getMaxAttempts() { return maxAttempts; }
    public long getBaseDelayMillis() { return baseDelayMillis; }
    public double getBackoffMultiplier() { return backoffMultiplier; }
    public long getTimeoutMillis() { return timeoutMillis; }
    public double getEmailFailRate() { return emailFailRate; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void patch(Integer maxAttempts, Long baseDelayMillis, Double backoffMultiplier, Long timeoutMillis, Double emailFailRate) {
        if (maxAttempts != null) this.maxAttempts = maxAttempts;
        if (baseDelayMillis != null) this.baseDelayMillis = baseDelayMillis;
        if (backoffMultiplier != null) this.backoffMultiplier = backoffMultiplier;
        if (timeoutMillis != null) this.timeoutMillis = timeoutMillis;
        if (emailFailRate != null) this.emailFailRate = emailFailRate;
        this.updatedAt = LocalDateTime.now();
    }
}