package org.backend.billing.message.service;

import java.time.LocalTime;
import java.util.Map;

import org.backend.billing.message.dto.request.TimeUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class TimeService {

    private final InMemoryStores stores;

    public TimeService(InMemoryStores stores) {
        this.stores = stores;
    }

    // 1) 금지시간 조회
    public Map<String, Object> get() {
        return Map.of(
                "enabled", stores.dndConfig.enabled,               // ✅ 추가
                "startTime", stores.dndConfig.start.toString(),
                "endTime", stores.dndConfig.end.toString(),
                "updatedAt", stores.dndConfig.updatedAt.toString(),
                "dndNow", isDndNow()
        );
    }

    // 2) 금지시간 설정(초기세팅/업데이트)
    // - 기존 TimeUpdateRequest 유지 + enabled도 받도록 권장
    public Map<String, Object> update(TimeUpdateRequest req) {
        // enabled가 req에 없다면: 아래 줄은 지워도 됨(그 대신 enable/disable 메서드로만 제어)
        if (req.enabled() != null) {
            stores.dndConfig.enabled = req.enabled();
        }

        stores.dndConfig.start = LocalTime.parse(req.startTime());
        stores.dndConfig.end = LocalTime.parse(req.endTime());
        stores.dndConfig.updatedAt = java.time.LocalDateTime.now();
        return get();
    }

    // 금지시간 ON/OFF만 별도로 제어(컨트롤러에서 쓰기 좋음)
    public Map<String, Object> enable() {
        stores.dndConfig.enabled = true;
        stores.dndConfig.updatedAt = java.time.LocalDateTime.now();
        return get();
    }

    public Map<String, Object> disable() {
        stores.dndConfig.enabled = false;
        stores.dndConfig.updatedAt = java.time.LocalDateTime.now();
        return get();
    }

    public boolean isDndNow() {
        if (!stores.dndConfig.enabled) return false; // ✅ 추가

        LocalTime now = LocalTime.now();
        LocalTime start = stores.dndConfig.start;
        LocalTime end = stores.dndConfig.end;

        // 22:00~08:00 같이 자정 걸치는 케이스 처리
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        } else {
            return !now.isBefore(start) || now.isBefore(end);
        }
    }

    // 3) 금지시간 보류 큐 상태 조회
    public Map<String, Object> queueStatus() {
        return Map.of("queuedCount", stores.dndQueue.size());
    }
}
