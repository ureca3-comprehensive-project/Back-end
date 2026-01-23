package org.backend.billing.message.entity;

import jakarta.persistence.*;
import org.backend.billing.message.type.AttemptStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "message_attempt",
        indexes = {
                @Index(name = "idx_attempt_message_id", columnList = "message_id")
        }
)
public class MessageAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Column(length = 30)
    private String provider;

    @Column(name = "provider_message_id", length = 80)
    private String providerMessageId;

    @Lob
    @Column(name = "request_payload")
    private String requestPayload;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "fail_code", length = 50)
    private String failCode;

    @Lob
    @Column(name = "fail_reason")
    private String failReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected MessageAttemptEntity() {}

    public MessageAttemptEntity(
            Long messageId,
            int attemptNo,
            AttemptStatus status,
            String provider,
            String providerMessageId,
            String requestPayload,
            Integer httpStatus,
            String failCode,
            String failReason,
            LocalDateTime requestedAt,
            LocalDateTime respondedAt
    ) {
        this.messageId = messageId;
        this.attemptNo = attemptNo;
        this.status = status;
        this.provider = provider;
        this.providerMessageId = providerMessageId;
        this.requestPayload = requestPayload;
        this.httpStatus = httpStatus;
        this.failCode = failCode;
        this.failReason = failReason;
        this.requestedAt = requestedAt;
        this.respondedAt = respondedAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getAttemptId() { return attemptId; }
    public Long getMessageId() { return messageId; }
    public int getAttemptNo() { return attemptNo; }
    public AttemptStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public Integer getHttpStatus() { return httpStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}