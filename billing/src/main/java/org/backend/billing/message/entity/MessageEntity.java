package org.backend.billing.message.entity;

import java.time.LocalDateTime;

import org.backend.billing.message.type.MessageStatus;
import org.backend.billing.message.type.MessageType;
import org.backend.billing.message.type.MessageTypeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "message",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_message_dedup_key", columnNames = "dedup_key")
        },
        indexes = {
                @Index(name = "idx_message_user_id", columnList = "user_id"),
                @Index(name = "idx_message_status", columnList = "status"),
                @Index(name = "idx_message_created_at", columnList = "created_at"),
                @Index(name = "idx_message_scheduled_at", columnList = "scheduled_at")
        }
)
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 너 DB 설계 기준
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "template_id")
    private Long templateId;

    @Convert(converter = MessageTypeConverter.class)
    @Column(name = "type", nullable = false)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status;

    @Column(name = "correlation_id", nullable = false, length = 80)
    private String correlationId;

    @Column(name = "dedup_key", nullable = false, length = 120)
    private String dedupKey;

    // 기존 코드/프론트 필요
    @Column(nullable = false, length = 255)
    private String destination;

    @Lob
    @Column(name = "variables_json")
    private String variablesJson;

    @Column(length = 255)
    private String subject;

    @Lob
    private String content;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // 현재는 DB 설계에 있지만, 네가 “관리자 DND 설정 안함”이라고 해서
    // 실제 로직에서 DND/available_at을 사용하지 않도록 서비스에서 제거했어.
    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retry", nullable = false)
    private int maxRetry;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    protected MessageEntity() {}
    
    public void markDndHold() {
        this.status = MessageStatus.DND_HOLD;
    }

    public void releaseDndHold() {
        this.status = MessageStatus.PENDING;
        this.availableAt = LocalDateTime.now();
    }
    public MessageEntity(
            Long userId,
            Long invoiceId,
            Long templateId,
            MessageType type,
            String destination,
            String variablesJson,
            String subject,
            String content,
            LocalDateTime scheduledAt,
            String correlationId,
            String dedupKey,
            int maxRetry
    ) {
        this.userId = userId;
        this.invoiceId = invoiceId;
        this.templateId = templateId;
        this.type = type;
        this.destination = destination;
        this.variablesJson = variablesJson;
        this.subject = subject;
        this.content = content;
        this.scheduledAt = scheduledAt;
        this.correlationId = correlationId;
        this.dedupKey = dedupKey;

        this.status = (scheduledAt != null) ? MessageStatus.PENDING : MessageStatus.PENDING;
        this.availableAt = (scheduledAt != null) ? scheduledAt : LocalDateTime.now();
        this.retryCount = 0;
        this.maxRetry = maxRetry;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getInvoiceId() { return invoiceId; }
    public Long getTemplateId() { return templateId; }
    public MessageType getType() { return type; }
    public MessageStatus getStatus() { return status; }
    public String getCorrelationId() { return correlationId; }
    public String getDedupKey() { return dedupKey; }
    public String getDestination() { return destination; }
    public String getVariablesJson() { return variablesJson; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetry() { return maxRetry; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void markSending() { this.status = MessageStatus.SENDING; }
    public void markSent() { this.status = MessageStatus.SENT; this.sentAt = LocalDateTime.now(); }
    public void markFailed() { this.status = MessageStatus.FAILED; }
    public void cancel() { this.status = MessageStatus.CANCELED; }
    public void markDuplicate() { this.status = MessageStatus.DUPLICATE; }

    public void incRetry() { this.retryCount += 1; }
}