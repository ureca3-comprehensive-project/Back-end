package org.backend.billing.message.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_dnd_config")
public class UserDndConfigEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime = LocalTime.parse("22:00");

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime = LocalTime.parse("08:00");

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected UserDndConfigEntity() {}

    public UserDndConfigEntity(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public boolean isEnabled() { return enabled; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void patch(Boolean enabled, String startTime, String endTime) {
        if (enabled != null) this.enabled = enabled;
        if (startTime != null) this.startTime = LocalTime.parse(startTime);
        if (endTime != null) this.endTime = LocalTime.parse(endTime);
        this.updatedAt = LocalDateTime.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
}