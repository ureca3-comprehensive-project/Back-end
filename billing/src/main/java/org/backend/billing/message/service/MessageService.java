package org.backend.billing.message.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.backend.billing.common.exception.ApiException;
import org.backend.billing.common.exception.ErrorCode;
import org.backend.billing.message.dto.request.MessageSendRequest;
import org.backend.billing.message.dto.request.ResendRequest;
import org.backend.billing.message.dto.request.SendEmailRequest;
import org.backend.billing.message.dto.request.SendPhoneRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.entity.MessageAttemptEntity;
import org.backend.billing.message.entity.MessageEntity;
import org.backend.billing.message.repository.MessageAttemptRepository;
import org.backend.billing.message.repository.MessageRepository;
import org.backend.billing.message.type.AttemptStatus;
import org.backend.billing.message.type.MessageStatus;
import org.backend.billing.message.type.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageAttemptRepository attemptRepository;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageService(MessageRepository messageRepository,
                          MessageAttemptRepository attemptRepository,
                          TemplateService templateService) {
        this.messageRepository = messageRepository;
        this.attemptRepository = attemptRepository;
        this.templateService = templateService;
    }

    // ===== Mock Send =====

    @Transactional
    public Map<String, Object> sendSmsMock(SendPhoneRequest req) {
        // mock은 clientRequestId로 dedupKey 강제
        String dedupKey = dedupKeyOrThrow(req.clientRequestId());

        MessageEntity m = new MessageEntity(
                0L,                // userId 없으면 0
                0L,                // invoiceId 없으면 0 (NOT NULL 대응)
                null,
                MessageType.SMS,
                req.phone(),
                toJson(Map.of()),
                null,
                req.content(),
                null,
                UUID.randomUUID().toString(),
                dedupKey,
                3
        );

        messageRepository.save(m);
        sendWithRetry(m.getId(), false, 0.0);
        return Map.of("messageId", m.getId(), "status", m.getStatus().name());
    }

    @Transactional
    public Map<String, Object> sendEmailMock(SendEmailRequest req) {
        String dedupKey = dedupKeyOrThrow(req.clientRequestId());

        MessageEntity m = new MessageEntity(
                0L,
                0L,
                null,
                MessageType.EMAIL,
                req.email(),
                toJson(Map.of()),
                req.subject(),
                req.content(),
                null,
                UUID.randomUUID().toString(),
                dedupKey,
                3
        );

        messageRepository.save(m);
        sendWithRetry(m.getId(), true, 0.0);
        return Map.of("messageId", m.getId(), "status", m.getStatus().name());
    }

    // ===== Main Send =====

    @Transactional
    public Map<String, Object> createSend(MessageSendRequest req) {
        MessageType type;
        try {
            type = MessageType.valueOf(req.channel().toUpperCase());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "invalid channel: " + req.channel());
        }

        String subject = null;
        String content;

        if (req.templateId() != null) {
            var preview = templateService.preview(new TemplatePreviewRequest(req.templateId(), req.variables()));
            subject = preview.get("subject");
            content = preview.get("body");
        } else {
            content = (req.variables() != null) ? req.variables().toString() : "";
        }

        // ✅ DTO에 dedupKey가 없으므로 UUID 생성
        String dedupKey = UUID.randomUUID().toString();

        MessageEntity m = new MessageEntity(
                req.userId(),
                0L, // ✅ invoiceId가 DTO에 없어서 0으로 저장 (원하면 DTO에 추가 추천)
                req.templateId(),
                type,
                req.destination(),
                toJson(Optional.ofNullable(req.variables()).orElse(Map.of())),
                subject,
                content,
                req.scheduledAt(),
                UUID.randomUUID().toString(),
                dedupKey,
                3 // ✅ maxRetry 기본값
        );

        messageRepository.save(m);

        // ✅ scheduledAt이 없거나 지금 <= 이면 즉시 발송
        if (req.scheduledAt() == null || !req.scheduledAt().isAfter(LocalDateTime.now())) {
            sendWithRetry(m.getId(), type == MessageType.EMAIL, 0.0);
        }

        return Map.of("messageId", m.getId(), "status", m.getStatus().name());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatus(Long userId) {
        long total = messageRepository.countByUserId(userId);
        long success = messageRepository.countByUserIdAndStatus(userId, MessageStatus.SENT);
        long fail = messageRepository.countByUserIdAndStatus(userId, MessageStatus.FAILED);
        long sending = messageRepository.countByUserIdAndStatus(userId, MessageStatus.SENDING);
        return Map.of("userId", userId, "total", total, "success", success, "fail", fail, "sending", sending);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        return messageRepository.findAllByOrderByCreatedAtAsc()
                .stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMessage(Long messageId) {
        MessageEntity m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId));
        return toMap(m);
    }

    @Transactional
    public Map<String, Object> cancel(Long messageId) {
        MessageEntity m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId));

        if (m.getStatus() == MessageStatus.SENT || m.getStatus() == MessageStatus.FAILED) {
            throw new ApiException(ErrorCode.CONFLICT, "already finished: " + m.getStatus());
        }
        m.cancel();
        return Map.of("messageId", m.getId(), "status", m.getStatus().name());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> failures() {
        return messageRepository.findByStatusOrderByCreatedAtAsc(MessageStatus.FAILED)
                .stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> scheduledList() {
        return messageRepository.findByScheduledAtIsNotNullOrderByScheduledAtAsc()
                .stream().map(this::toMap).toList();
    }

    // InMemory 기반 DND flush는 제거(관리자 설정 안쓴다고 했으니 0 반환)
    @Transactional(readOnly = true)
    public Map<String, Object> flushDndQueue() {
        return Map.of("flushed", 0, "queuedLeft", 0);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> history() {
        List<MessageEntity> msgs = messageRepository.findAllByOrderByCreatedAtAsc();
        List<Map<String, Object>> out = new ArrayList<>();
        for (MessageEntity m : msgs) {
            for (MessageAttemptEntity a : attemptRepository.findTop200ByMessageIdOrderByAttemptNoAsc(m.getId())) {
                out.add(Map.of(
                        "attemptId", a.getAttemptId(),
                        "messageId", a.getMessageId(),
                        "attemptNo", a.getAttemptNo(),
                        "status", a.getStatus().name(),
                        "provider", a.getProvider(),
                        "httpStatus", a.getHttpStatus(),
                        "createdAt", a.getCreatedAt().toString()
                ));
            }
        }
        return out;
    }

    @Transactional
    public Map<String, Object> resend(ResendRequest req) {
        MessageEntity origin = messageRepository.findById(req.messageId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "message not found: " + req.messageId()));

        if (origin.getType() != MessageType.EMAIL) throw new ApiException(ErrorCode.BAD_REQUEST, "origin is not EMAIL");
        if (origin.getStatus() != MessageStatus.FAILED) throw new ApiException(ErrorCode.CONFLICT, "origin status is not FAILED");

        MessageEntity sms = new MessageEntity(
                origin.getUserId(),
                origin.getInvoiceId(),
                origin.getTemplateId(),
                MessageType.SMS,
                req.fallbackPhone(),
                origin.getVariablesJson(),
                null,
                "[대체발송] " + Optional.ofNullable(origin.getContent()).orElse(""),
                null,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                3
        );

        messageRepository.save(sms);
        sendWithRetry(sms.getId(), false, 0.0);

        return Map.of("fallbackMessageId", sms.getId(), "status", sms.getStatus().name());
    }

    @Transactional
    public Map<String, Object> sendByType(String type, MessageSendRequest req) {
        String ch = type.toUpperCase();
        MessageSendRequest fixed = new MessageSendRequest(req.userId(), ch, req.destination(), req.templateId(), req.variables(), req.scheduledAt());
        return createSend(fixed);
    }

    @Transactional
    public Map<String, Object> emailErrorTest(SendEmailRequest req) {
        String dedupKey = dedupKeyOrThrow(req.clientRequestId());

        MessageEntity m = new MessageEntity(
                0L, 0L, null,
                MessageType.EMAIL,
                req.email(),
                toJson(Map.of()),
                req.subject(),
                req.content(),
                null,
                UUID.randomUUID().toString(),
                dedupKey,
                3
        );
        messageRepository.save(m);

        // 이메일 1% 실패
        sendWithRetry(m.getId(), true, 0.01);

        return Map.of("messageId", m.getId(), "status", m.getStatus().name());
    }

    // ===== 내부 발송(Mock) =====

    private void sendWithRetry(Long messageId, boolean isEmail, double emailFailRate) {
        MessageEntity m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId));

        if (m.getStatus() == MessageStatus.CANCELED) return;

        if (m.getScheduledAt() != null && m.getScheduledAt().isAfter(LocalDateTime.now())) {
            return;
        }

        m.markSending();

        int maxAttempts = m.getMaxRetry();
        for (int attemptNo = 1; attemptNo <= maxAttempts; attemptNo++) {

            recordAttempt(m.getId(), attemptNo, AttemptStatus.ATTEMPTING, providerOf(m.getType()), 0);
            sleep(1000);

            boolean ok = simulateProvider(isEmail, emailFailRate);

            if (ok) {
                recordAttempt(m.getId(), attemptNo, AttemptStatus.SUCCESS, providerOf(m.getType()), 200);
                m.markSent();
                return;
            } else {
                recordAttempt(m.getId(), attemptNo, AttemptStatus.FAIL, providerOf(m.getType()), 500);
                m.incRetry();
                if (attemptNo == maxAttempts) {
                    m.markFailed();
                    return;
                }
                sleep(300L * attemptNo);
            }
        }
    }

    private boolean simulateProvider(boolean isEmail, double emailFailRate) {
        if (!isEmail) return true;
        return Math.random() >= emailFailRate;
    }

    private String providerOf(MessageType type) {
        return switch (type) {
            case EMAIL -> "mock-smtp";
            case SMS -> "mock-sms-gw";
            case PUSH -> "mock-push";
            case ETC -> "mock-etc";
        };
    }

    private void recordAttempt(Long messageId, int attemptNo, AttemptStatus status, String provider, int httpStatus) {
        MessageAttemptEntity a = new MessageAttemptEntity(
                messageId,
                attemptNo,
                status,
                provider,
                "prov-" + UUID.randomUUID(),
                null,
                httpStatus,
                status == AttemptStatus.FAIL ? "PROVIDER_ERROR" : null,
                status == AttemptStatus.FAIL ? "Mock provider failed" : null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        attemptRepository.save(a);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }

    private String dedupKeyOrThrow(String clientRequestId) {
        if (clientRequestId == null || clientRequestId.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "clientRequestId is required");
        }
        if (messageRepository.findByDedupKey(clientRequestId).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "duplicate request: " + clientRequestId);
        }
        return clientRequestId;
    }
    @Transactional
    public Map<String, Object> flushDndHold(Long userId) {
        List<MessageEntity> targets =
                messageRepository.findTop200ByUserIdAndStatusOrderByCreatedAtAsc(userId, MessageStatus.DND_HOLD);

        for (MessageEntity m : targets) {
            m.releaseDndHold(); // DND_HOLD -> PENDING, availableAt=now
        }

        long left = messageRepository.countByUserIdAndStatus(userId, MessageStatus.DND_HOLD);

        return Map.of(
                "userId", userId,
                "flushed", targets.size(),
                "queuedLeft", left
        );
    }
    
    private Map<String, Object> toMap(MessageEntity m) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("userId", m.getUserId());
        out.put("invoiceId", m.getInvoiceId());
        out.put("type", m.getType().name());
        out.put("destination", m.getDestination());
        out.put("templateId", m.getTemplateId());
        out.put("status", m.getStatus().name());

        out.put("scheduledAt", m.getScheduledAt() == null ? null : m.getScheduledAt().toString());
        out.put("sentAt", m.getSentAt() == null ? null : m.getSentAt().toString());
        out.put("createdAt", m.getCreatedAt() == null ? null : m.getCreatedAt().toString());
        out.put("updatedAt", m.getUpdatedAt() == null ? null : m.getUpdatedAt().toString());

        return out;
    }
}