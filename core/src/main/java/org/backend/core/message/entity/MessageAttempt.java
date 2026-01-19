package org.backend.core.message.entity;

import java.time.LocalDateTime;

import org.backend.core.common.entity.BaseEntity;
import org.backend.core.message.type.MessageAttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "message_attempt")
public class MessageAttempt extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private Message message;
	
	@Column
	private Integer attemptNo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageAttemptStatus status;
	
	@Column(nullable = true, length = 50)
	private String failCode;
	
	@Column(nullable = true, length = 255)
	private String failReason;
	
	@Column(name = "requested_at")
	private LocalDateTime requestedAt;
	
	@Column(name = "responded_at")
	private LocalDateTime respondedAt;
	
	
	
	
	

}
