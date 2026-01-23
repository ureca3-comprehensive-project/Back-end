package org.backend.billing.message.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryStores {

    // ====== MODELS ======
    public enum Channel { EMAIL, SMS, PUSH }
    public enum MessageStatus { REQUESTED, SCHEDULED, QUEUED, SENDING, SUCCESS, FAIL, CANCELLED }

    public record Template(
            Long id,
            String name,
            Channel channel,
            String subjectTemplate,
            String bodyTemplate,
            List<String> allowedVariables,
            int version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record Attempt(
            Long id,
            Long messageId,
            int attemptNo,
            String status, // ATTEMPTING / SUCCESS / FAIL
            String provider,
            String providerMessageId,
            int httpStatus,
            LocalDateTime createdAt
    ) {}

    public static class Message {
        public Long id;
        public Long userId;
        public Channel channel;
        public String destination;
        public Long templateId;
        public Map<String, String> variables;
        public String subject;
        public String content;
        public MessageStatus status;
        public LocalDateTime scheduledAt;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public Message(Long id) { this.id = id; }
    }

    public static class DndConfig {
        public boolean enabled = true;   // ✅ 추가 (기본 ON 추천)
        public LocalTime start = LocalTime.parse("22:00");
        public LocalTime end = LocalTime.parse("08:00");
        public LocalDateTime updatedAt = LocalDateTime.now();
    }

    public static class RetryPolicy {
        public int maxAttempts = 3;
        public long baseDelayMillis = 300;
        public double backoffMultiplier = 2.0;
        public long timeoutMillis = 3000;
        public double emailFailRate = 0.01; // 1%
        public LocalDateTime updatedAt = LocalDateTime.now();
    }

    // ====== STORES ======
    public final AtomicLong templateSeq = new AtomicLong(0);
    public final ConcurrentHashMap<Long, Template> templates = new ConcurrentHashMap<>();

    public final AtomicLong messageSeq = new AtomicLong(0);
    public final ConcurrentHashMap<Long, Message> messages = new ConcurrentHashMap<>();

    public final AtomicLong attemptSeq = new AtomicLong(0);
    public final CopyOnWriteArrayList<Attempt> attempts = new CopyOnWriteArrayList<>();

    // 금지시간 큐(메시지ID)
    public final ConcurrentLinkedQueue<Long> dndQueue = new ConcurrentLinkedQueue<>();

    // 중복 방지용 clientRequestId
    public final ConcurrentHashMap<String, Long> dedupe = new ConcurrentHashMap<>();

    public final DndConfig dndConfig = new DndConfig();
    public final RetryPolicy retryPolicy = new RetryPolicy();

    // 스케줄링
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    public final ExecutorService asyncExecutor = Executors.newFixedThreadPool(8);
}