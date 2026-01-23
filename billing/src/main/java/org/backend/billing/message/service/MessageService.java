package org.backend.billing.message.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.backend.billing.common.exception.ApiException;
import org.backend.billing.common.exception.ErrorCode;
import org.backend.billing.message.dto.request.MessageSendRequest;
import org.backend.billing.message.dto.request.ResendRequest;
import org.backend.billing.message.dto.request.SendEmailRequest;
import org.backend.billing.message.dto.request.SendPhoneRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.service.InMemoryStores.Attempt;
import org.backend.billing.message.service.InMemoryStores.Channel;
import org.backend.billing.message.service.InMemoryStores.Message;
import org.backend.billing.message.service.InMemoryStores.MessageStatus;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final InMemoryStores stores;
    private final TimeService timeService;
    private final TemplateService templateService;

    public MessageService(InMemoryStores stores, TimeService timeService, TemplateService templateService) {
        this.stores = stores;
        this.timeService = timeService;
        this.templateService = templateService;
    }

    // sms mock 발송
    public Map<String, Object> sendSmsMock(SendPhoneRequest req) {
        dedupe(req.clientRequestId());
        Message m = newMessage(null, Channel.SMS, req.phone(), null, Map.of(), null, req.content(), null);
        stores.messages.put(m.id, m);
        asyncSend(m.id, false);
        return Map.of("messageId", m.id, "status", m.status.name());
    }

    // email mock 발송
    public Map<String, Object> sendEmailMock(SendEmailRequest req) {
        dedupe(req.clientRequestId());
        Message m = newMessage(null, Channel.EMAIL, req.email(), null, Map.of(), req.subject(), req.content(), null);
        stores.messages.put(m.id, m);
        asyncSend(m.id, true);
        return Map.of("messageId", m.id, "status", m.status.name());
    }

    // 발송 요청 생성 (즉시 or 예약)
    public Map<String, Object> createSend(MessageSendRequest req) {
        Channel ch = Channel.valueOf(req.channel().toUpperCase());

        String subject = null;
        String content = null;

        if (req.templateId() != null) {
            var preview = templateService.preview(new TemplatePreviewRequest(req.templateId(), req.variables()));
            subject = preview.get("subject");
            content = preview.get("body");
        } else {
            content = req.variables() != null ? req.variables().toString() : "";
        }

        Message m = newMessage(req.userId(), ch, req.destination(), req.templateId(),
                req.variables() == null ? Map.of() : req.variables(),
                subject, content, req.scheduledAt());

        stores.messages.put(m.id, m);

        if (req.scheduledAt() != null) {
            schedule(m.id, req.scheduledAt());
            m.status = MessageStatus.SCHEDULED;
            touch(m);
        } else {
            asyncSend(m.id, ch == Channel.EMAIL);
        }

        return Map.of("messageId", m.id, "status", m.status.name());
    }

    // 사용자 상태 조회(명세 충돌 회피용)
    public Map<String, Object> getUserStatus(Long userId) {
        long total = stores.messages.values().stream().filter(x -> Objects.equals(x.userId, userId)).count();
        long success = stores.messages.values().stream().filter(x -> Objects.equals(x.userId, userId) && x.status == MessageStatus.SUCCESS).count();
        long fail = stores.messages.values().stream().filter(x -> Objects.equals(x.userId, userId) && x.status == MessageStatus.FAIL).count();
        long queued = stores.messages.values().stream().filter(x -> Objects.equals(x.userId, userId) && x.status == MessageStatus.QUEUED).count();
        return Map.of("userId", userId, "total", total, "success", success, "fail", fail, "queued", queued);
    }

    public List<Map<String, Object>> list() {
        return stores.messages.values().stream()
                .sorted(Comparator.comparing(x -> x.createdAt))
                .map(this::toMap)
                .toList();
    }

    public Map<String, Object> getMessage(Long messageId) {
        Message m = stores.messages.get(messageId);
        if (m == null) throw new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId);
        return toMap(m);
    }

    public Map<String, Object> cancel(Long messageId) {
        Message m = stores.messages.get(messageId);
        if (m == null) throw new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId);

        if (m.status == MessageStatus.SUCCESS || m.status == MessageStatus.FAIL) {
            throw new ApiException(ErrorCode.CONFLICT, "already finished: " + m.status);
        }

        m.status = MessageStatus.CANCELLED;
        touch(m);
        return Map.of("messageId", m.id, "status", m.status.name());
    }

    public List<Map<String, Object>> history() {
        return stores.attempts.stream()
            .map(a -> Map.<String, Object>of(
                    "attemptId", a.id(),
                    "messageId", a.messageId(),
                    "attemptNo", a.attemptNo(),
                    "status", a.status(),
                    "provider", a.provider(),
                    "httpStatus", a.httpStatus(),
                    "createdAt", a.createdAt().toString()
            ))
            .toList();
    }

    public List<Map<String, Object>> failures() {
        return stores.messages.values().stream()
                .filter(m -> m.status == MessageStatus.FAIL)
                .map(this::toMap)
                .toList();
    }

    // 예약 발송 목록
    public List<Map<String, Object>> scheduledList() {
        return stores.messages.values().stream()
                .filter(m -> m.status == MessageStatus.SCHEDULED)
                .map(this::toMap)
                .toList();
    }

    // 금지시간 큐 flush
    public Map<String, Object> flushDndQueue() {
        int moved = 0;
        while (true) {
            Long id = stores.dndQueue.poll();
            if (id == null) break;
            Message m = stores.messages.get(id);
            if (m == null) continue;
            if (m.status != MessageStatus.QUEUED) continue;
            asyncSend(m.id, m.channel == Channel.EMAIL);
            moved++;
        }
        return Map.of("flushed", moved, "queuedLeft", stores.dndQueue.size());
    }

    // 이메일 실패시 SMS 대체 발송
    public Map<String, Object> resend(ResendRequest req) {
        Message origin = stores.messages.get(req.messageId());
        if (origin == null) throw new ApiException(ErrorCode.NOT_FOUND, "message not found: " + req.messageId());
        if (origin.channel != Channel.EMAIL) throw new ApiException(ErrorCode.BAD_REQUEST, "origin is not EMAIL");
        if (origin.status != MessageStatus.FAIL) throw new ApiException(ErrorCode.CONFLICT, "origin status is not FAIL");

        Message sms = newMessage(origin.userId, Channel.SMS, req.fallbackPhone(), origin.templateId, origin.variables,
                null, "[대체발송] " + origin.content, null);

        stores.messages.put(sms.id, sms);
        asyncSend(sms.id, false);

        return Map.of("fallbackMessageId", sms.id, "status", sms.status.name());
    }

    // /messages/send/{type}
    public Map<String, Object> sendByType(String type, MessageSendRequest req) {
        String ch = type.toUpperCase();
        MessageSendRequest fixed = new MessageSendRequest(req.userId(), ch, req.destination(), req.templateId(), req.variables(), req.scheduledAt());
        return createSend(fixed);
    }

    // /messages/error (이메일 1% 실패 테스트)
    public Map<String, Object> emailErrorTest(SendEmailRequest req) {
        dedupe(req.clientRequestId());
        Message m = newMessage(null, Channel.EMAIL, req.email(), null, Map.of(), req.subject(), req.content(), null);
        stores.messages.put(m.id, m);
        asyncSend(m.id, true); // failRate는 retryPolicy.emailFailRate 사용
        return Map.of("messageId", m.id, "status", m.status.name());
    }

    // ====== INTERNAL ======

    private void schedule(Long messageId, LocalDateTime when) {
        long delayMs = Math.max(0, Duration.between(LocalDateTime.now(), when).toMillis());
        stores.scheduler.schedule(() -> asyncSend(messageId, true), delayMs, TimeUnit.MILLISECONDS);
    }

    private void asyncSend(Long messageId, boolean isEmail) {
        stores.asyncExecutor.submit(() -> sendWithRetry(messageId, isEmail));
    }

    private void sendWithRetry(Long messageId, boolean isEmail) {
        Message m = stores.messages.get(messageId);
        if (m == null) return;
        if (m.status == MessageStatus.CANCELLED) return;

        // 금지시간이면 큐로
        if (timeService.isDndNow()) {
            m.status = MessageStatus.QUEUED;
            touch(m);
            stores.dndQueue.add(m.id);
            return;
        }

        m.status = MessageStatus.SENDING;
        touch(m);

        var policy = stores.retryPolicy;
        long delay = policy.baseDelayMillis;

        for (int attemptNo = 1; attemptNo <= policy.maxAttempts; attemptNo++) {
            if (m.status == MessageStatus.CANCELLED) return;

            recordAttempt(m.id, attemptNo, "ATTEMPTING", providerOf(m.channel), 0);

            // 1초 딜레이(요구사항 반영)
            sleep(1000);

            boolean ok = simulateProvider(isEmail, policy.emailFailRate);

            if (ok) {
                recordAttempt(m.id, attemptNo, "SUCCESS", providerOf(m.channel), 200);
                m.status = MessageStatus.SUCCESS;
                touch(m);
                return;
            } else {
                recordAttempt(m.id, attemptNo, "FAIL", providerOf(m.channel), 500);
                if (attemptNo == policy.maxAttempts) {
                    m.status = MessageStatus.FAIL;
                    touch(m);
                    return;
                }
                sleep(delay);
                delay = (long) (delay * policy.backoffMultiplier);
            }
        }
    }

    private boolean simulateProvider(boolean isEmail, double emailFailRate) {
        // emailFailRate (ex 0.01) 실패 확률
        if (!isEmail) return true; // SMS/PUSH는 mock에선 성공 처리
        return Math.random() >= emailFailRate;
    }

    private String providerOf(Channel ch) {
        return switch (ch) {
            case EMAIL -> "mock-smtp";
            case SMS -> "mock-sms-gw";
            case PUSH -> "mock-push";
        };
    }

    private void recordAttempt(Long messageId, int attemptNo, String status, String provider, int httpStatus) {
        long id = stores.attemptSeq.incrementAndGet();
        stores.attempts.add(new Attempt(
                id, messageId, attemptNo, status, provider,
                "prov-" + UUID.randomUUID(), httpStatus, LocalDateTime.now()
        ));
    }

    private Message newMessage(Long userId, Channel ch, String dest, Long templateId,
                               Map<String, String> vars, String subject, String content, LocalDateTime scheduledAt) {
        long id = stores.messageSeq.incrementAndGet();
        Message m = new Message(id);
        m.userId = userId;
        m.channel = ch;
        m.destination = dest;
        m.templateId = templateId;
        m.variables = vars;
        m.subject = subject;
        m.content = content;
        m.status = MessageStatus.REQUESTED;
        m.scheduledAt = scheduledAt;
        m.createdAt = LocalDateTime.now();
        m.updatedAt = m.createdAt;
        return m;
    }

    private void touch(Message m) {
        m.updatedAt = LocalDateTime.now();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void dedupe(String clientRequestId) {
        if (clientRequestId == null || clientRequestId.isBlank()) return;
        Long prev = stores.dedupe.putIfAbsent(clientRequestId, -1L);
        if (prev != null) throw new ApiException(ErrorCode.CONFLICT, "duplicate request: " + clientRequestId);
    }

    private Map<String, Object> toMap(Message m) {
        return Map.of(
                "id", m.id,
                "userId", m.userId,
                "channel", m.channel.name(),
                "destination", m.destination,
                "templateId", m.templateId,
                "status", m.status.name(),
                "scheduledAt", m.scheduledAt == null ? null : m.scheduledAt.toString(),
                "createdAt", m.createdAt.toString(),
                "updatedAt", m.updatedAt.toString()
        );
    }
}