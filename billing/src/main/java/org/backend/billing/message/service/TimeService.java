package org.backend.billing.message.service;

import java.time.LocalTime;
import java.util.Map;

import org.backend.billing.message.dto.request.TimeUpdateRequest;
import org.backend.billing.message.entity.UserDndConfigEntity;
import org.backend.billing.message.repository.MessageRepository;
import org.backend.billing.message.repository.UserDndConfigRepository;
import org.backend.billing.message.type.MessageStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeService {

    private final UserDndConfigRepository dndRepo;
    private final MessageRepository messageRepository;

    public TimeService(UserDndConfigRepository dndRepo, MessageRepository messageRepository) {
        this.dndRepo = dndRepo;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(Long userId) {
        UserDndConfigEntity cfg = getOrCreate(userId);
        return Map.of(
                "userId", userId,
                "enabled", cfg.isEnabled(),
                "startTime", cfg.getStartTime().toString(),
                "endTime", cfg.getEndTime().toString(),
                "updatedAt", cfg.getUpdatedAt().toString(),
                "dndNow", isDndNow(userId)
        );
    }

    @Transactional
    public Map<String, Object> update(Long userId, TimeUpdateRequest req) {
        UserDndConfigEntity cfg = getOrCreate(userId);
        cfg.patch(req.enabled(), req.startTime(), req.endTime());
        return get(userId);
    }

    @Transactional
    public Map<String, Object> enable(Long userId) {
        UserDndConfigEntity cfg = getOrCreate(userId);
        cfg.enable();
        return get(userId);
    }

    @Transactional
    public Map<String, Object> disable(Long userId) {
        UserDndConfigEntity cfg = getOrCreate(userId);
        cfg.disable();
        return get(userId);
    }

    @Transactional(readOnly = true)
    public boolean isDndNow(Long userId) {
        UserDndConfigEntity cfg = getOrCreate(userId);
        if (!cfg.isEnabled()) return false;

        LocalTime now = LocalTime.now();
        LocalTime start = cfg.getStartTime();
        LocalTime end = cfg.getEndTime();

        // 22:00~08:00 (자정 걸치는 케이스)
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        } else {
            return !now.isBefore(start) || now.isBefore(end);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> queueStatus(Long userId) {
        long cnt = messageRepository.countByUserIdAndStatus(userId, MessageStatus.DND_HOLD);
        return Map.of("userId", userId, "queuedCount", cnt);
    }

    private UserDndConfigEntity getOrCreate(Long userId) {
        return dndRepo.findById(userId).orElseGet(() -> dndRepo.save(new UserDndConfigEntity(userId)));
    }
}