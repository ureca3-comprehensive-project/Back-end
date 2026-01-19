package org.backend.core.message.entity;

import java.time.LocalDateTime;

import org.backend.core.common.entity.BaseEntity;
import org.backend.core.message.type.ChannelType;
import org.backend.core.message.type.MessageStatus;
import org.backend.core.template.entity.Template;

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
@Table(name = "message",
        indexes = {
        })
public class Message extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChannelType channelType;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageStatus status;

	@Column(nullable = false, unique = true, length = 255)
	private String dedupKey;

	@Column(nullable = false, unique = true, length = 255)
	private String correlationId;
	
	@Column(nullable = false)
	private Integer retryCount = 0;

	@Column(nullable = false)
	private Integer maxRetry = 3;
	
	@Column(nullable = true)
	private LocalDateTime scheduledAt;

	@Column(nullable = true)
	private LocalDateTime availableAt;

	@Column(nullable = true)
	private LocalDateTime sentAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private Template template;
	
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "invoice_id", nullable = false)
//    private Invoice invoice;
//
//	@ManyToOne(fetch = FetchType.LAZY)
//    @Column(name = "template_id", nullable = false)
//    private Template template;

}
