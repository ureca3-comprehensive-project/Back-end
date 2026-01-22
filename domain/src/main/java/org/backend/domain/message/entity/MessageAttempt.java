package org.backend.domain.message.entity;

import java.time.LocalDateTime;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.message.type.MessageAttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "message_attempt")
public class MessageAttempt extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "attempt_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private Message message;
	
	@Column(name = "attempt_no")
	private Long attemptNo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status")
	private MessageAttemptStatus status;

	@Column(nullable = true, length = 80, name = "provider_message_id")
	private String providerMessageId;
	
	@Lob
	@Column(name = "request_payload")
    private String requestPayload;
	

	@Column(nullable = true, name = "http_status")
	private Integer httpStatus;
	
	
	@Column(nullable = true, length = 50, name = "fail_code")
	private String failCode;
	
	@Column(nullable = true, length = 255, name = "fail_reason")
	private String failReason;
	
	@Column(name = "requested_at")
	private LocalDateTime requestedAt;
	
	@Column(name = "responded_at")
	private LocalDateTime respondedAt;
	
	public static MessageAttempt attempting(Message message, long attemptNo, String payload ) {
        
		MessageAttempt a = new MessageAttempt();
        
		a.message = message;
        a.attemptNo = attemptNo;
        a.status = MessageAttemptStatus.ATTEMPTING;
        a.requestPayload = payload;
        a.requestedAt = LocalDateTime.now();
        
        return a;
    }

    public void success(String providerMessageId, int httpStatus) {
        this.status = MessageAttemptStatus.SUCCESS;
        this.providerMessageId = providerMessageId;
        this.httpStatus = httpStatus;
        this.respondedAt = LocalDateTime.now();
    }

    public void fail(String failCode, String failReason, Integer httpStatus) {
        this.status = MessageAttemptStatus.FAIL;
        this.failCode = failCode;
        this.failReason = failReason;
        this.httpStatus = httpStatus;
        this.respondedAt = LocalDateTime.now();
    }
	
	
	
	
	

}
