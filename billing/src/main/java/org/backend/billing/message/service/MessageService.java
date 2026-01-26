package org.backend.billing.message.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.backend.billing.common.exception.ApiException;
import org.backend.billing.common.exception.ErrorCode;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.repository.MessageAttemptRepository;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.message.type.MessageStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final MessageAttemptRepository attemptRepository;
	
	
	public List<Map<String, Object>> list(String status, String channel, String q){
		
		List<Message> base;

        if (notBlank(status) && notBlank(channel)) {
            base = messageRepository.findTop200ByStatusAndChannelTypeOrderByCreatedAtDesc(
                    parseStatus(status), parseChannel(channel)
            );
        } else if (notBlank(status)) {
            base = messageRepository.findTop200ByStatusOrderByCreatedAtDesc(parseStatus(status));
        } else if (notBlank(channel)) {
            base = messageRepository.findTop200ByChannelTypeOrderByCreatedAtDesc(parseChannel(channel));
        } else {
            base = messageRepository.findTop200ByOrderByCreatedAtDesc();
        }
        
        return base.stream().map(this::toListRow).toList();
	}
	
	 // =========================
    // 프론트(상세)
    // =========================
    @Transactional(readOnly = true)
    public Map<String, Object> getMessage(Long messageId) {
        Message m = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "message not found: " + messageId));

        List<MessageAttempt> attempts = attemptRepository.findTop200ByMessage_IdOrderByAttemptNoAsc(messageId);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("status", uiStatus(m.getStatus()));
        out.put("channel", m.getChannelType().name());
        out.put("provider", providerOf(m.getChannelType()));
        out.put("templateId", (m.getTemplate() == null) ? null : m.getTemplate().getId());
        out.put("createdAt", (m.getCreatedAt() == null) ? null : m.getCreatedAt().toString());

        // payload는 마지막 attempt의 requestPayload로
        String payload = attempts.isEmpty() ? null : attempts.get(attempts.size() - 1).getRequestPayload();
        out.put("payload", payload);

        // ✅ Map.of 쓰면 null 들어올 때 NPE -> LinkedHashMap으로 교체
        List<Map<String, Object>> attemptRows = new ArrayList<>();
        for (MessageAttempt a : attempts) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("no", a.getAttemptNo());
            r.put("status", a.getStatus().name());
            r.put("http", a.getHttpStatus());
            r.put("providerMsgId", a.getProviderMessageId()); // null 가능
            r.put("at", (a.getCreatedAt() == null) ? null : a.getCreatedAt().toString());
            attemptRows.add(r);
        }
        out.put("attempts", attemptRows);

        return out;
    }
	
	
    // =========================
    // Helpers
    // =========================
    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    
    private MessageStatus parseStatus(String s) {
        String u = s.toUpperCase();
        return switch (u) {
            case "SUCCESS", "SENT" -> MessageStatus.SENT;
            case "FAIL", "FAILED" -> MessageStatus.FAILED;
            default -> MessageStatus.PENDING;
        };
    }

    private ChannelType parseChannel(String s) {
        return ChannelType.valueOf(s.toUpperCase());
    }
    
    private String uiStatus(MessageStatus s) {
        return switch (s) {
            case SENT -> "SUCCESS";
            case FAILED -> "FAIL";
            default -> "RETRY";
        };
    }
    
    private String providerOf(ChannelType ch) {
        return switch (ch) {
            case EMAIL -> "mock-smtp";
            case SMS -> "mock-sms-gw";
            case PUSH -> "mock-push";
        };
    }
    
 // ✅ 여기가 500의 핵심 원인(기존 Map.of에 null 들어감) -> LinkedHashMap으로 수정
    private Map<String, Object> toListRow(Message m) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", m.getId());
        row.put("status", uiStatus(m.getStatus()));
        row.put("channel", m.getChannelType().name());
        row.put("provider", providerOf(m.getChannelType()));
        row.put("templateId", (m.getTemplate() == null) ? null : m.getTemplate().getId());
        row.put("createdAt", (m.getCreatedAt() == null) ? null : m.getCreatedAt().toString());
        return row;
    }
	
}
