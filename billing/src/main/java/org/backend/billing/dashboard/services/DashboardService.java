package org.backend.billing.dashboard.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.MessageStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final MessageRepository messageRepository;
    private final EntityManager entityManager;

    public Map<String, Object> getDashboardSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 오늘 생성된 청구서 수
        long todayBillingCount = invoiceRepository.countByCreatedAtAfter(todayStart);

        // 메시지 성공/실패/대기 건수
        long msgSuccess = messageRepository.countByStatusAndCreatedAtAfter(MessageStatus.SENT, todayStart);
        long msgFail = messageRepository.countByStatusAndCreatedAtAfter(MessageStatus.FAILED, todayStart);
        long msgPending = messageRepository.countByStatusAndCreatedAtAfter(MessageStatus.PENDING, todayStart);

        // 채널별 통계 초기화
        Map<String, Long> channelRatio = new HashMap<>();
        channelRatio.put("EMAIL", 0L);
        channelRatio.put("SMS", 0L);
        channelRatio.put("PUSH", 0L);

        // 채널별 발송 비율 계산
        List<Object[]> channelStats = messageRepository.countByChannelType();
        for (Object[] row : channelStats) {
            String type = row[0].toString();
            channelRatio.put(type, (Long) row[1]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayBillingCount", todayBillingCount);
        result.put("msgSuccess", msgSuccess);
        result.put("msgFail", msgFail);
        result.put("msgPending", msgPending); // Kafka 미처리량 모니터링용
        result.put("channelRatio", channelRatio);

        // 배치 요약 정보 - 배치를 바로 가져올 수 없기에 일단 그냥 더미로 설정함
        // 최근 Invoice 혹은 message의 시간을 마지막 실행 시간으로 간주하고자 함
        result.put("batch", Map.of(
                "lastStatus", "RUNNING",
                "running", 0
        ));

        return result;
    }

    public List<Map<String, Object>> getRecentFailures() {
        List<Message> failures = messageRepository.findTop5ByStatusOrderByCreatedAtDesc(MessageStatus.FAILED);

        return failures.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("messageId", m.getId());
                    map.put("channel", m.getChannelType());
                    map.put("provider", "System"); // 템플릿 정보로 사용하거나 제외
                    map.put("status", m.getStatus().toString());
                    map.put("errorCode", "SEND_ERROR"); // SEND_ERROR로만 우선 에러 처리
                    map.put("createdAt", m.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }
}