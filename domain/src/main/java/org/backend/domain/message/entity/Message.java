package org.backend.domain.message.entity;

import java.time.LocalDateTime;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.message.type.MessageStatus;
import org.backend.domain.template.entity.Template;

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
@Table(name = "message",
        indexes = {
        		@Index(name = "idx_message_invoice_id", columnList = "invoice_id")
        })
public class Message extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private Template template;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "channel_type")
	private ChannelType channelType;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status")
	private MessageStatus status;

	@Column(nullable = false, unique = true, length = 255, name = "dedup_key")
	private String dedupKey;

	@Column(nullable = false, unique = false, length = 255 , name = "correlation_id")
	private String correlationId;
	
	@Column(nullable = false , name = "retry_count")
	private Integer retryCount = 0;

	@Column(nullable = false , name = "max_retry")
	private Integer maxRetry = 3;

	@Column(nullable = true , name = "available_at")
	private LocalDateTime availableAt;

	@Column(nullable = true, name = "sent_at")
	private LocalDateTime sentAt;	
	
	
	
	/* Message Status 변경 메소드*/
	public void markPending() {
		this.status = MessageStatus.PENDING;
	}

	public void markSending() {
		this.status = MessageStatus.SENDING;
	}
	
	public void markSent() {
		this.status = MessageStatus.SENT;
		this.sentAt = LocalDateTime.now();
	}

	public void dndHold(LocalDateTime availableAt) {
		this.availableAt = availableAt;
		this.status = MessageStatus.DND_HOLD;
	}
	
	public void markFail() {
		this.status = MessageStatus.FAILED;
	}
	
	
	/* 재시도 관련 메소드 */
	public void increaseRetry() {
		this.retryCount++;
	}
	
	public void switchChannel(ChannelType channelType) {
		this.channelType = channelType;
	}

}
