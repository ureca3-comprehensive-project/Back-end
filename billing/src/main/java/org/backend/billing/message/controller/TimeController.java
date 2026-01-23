package org.backend.billing.message.controller;

import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.dto.request.TimeUpdateRequest;
import org.backend.billing.message.service.MessageService;
import org.backend.billing.message.service.TimeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages/time")
public class TimeController {

    private final TimeService timeService;
    private final MessageService messageService;

    public TimeController(TimeService timeService, MessageService messageService) {
        this.timeService = timeService;
        this.messageService = messageService;
    }

    // 1) 금지시간 조회
    @GetMapping("/dnd")
    public ApiResponse<Map<String, Object>> getDnd() {
        return ApiResponse.ok(timeService.get());
    }

    // 2) 금지시간 설정(초기세팅/업데이트)
    // 예: { "startTime": "22:00", "endTime": "08:00", "enabled": true }
    @PutMapping("/dnd")
    public ApiResponse<Map<String, Object>> updateDnd(@RequestBody TimeUpdateRequest req) {
        return ApiResponse.ok(timeService.update(req));
    }

    // (추가) ON
    @PostMapping("/dnd/enable")
    public ApiResponse<Map<String, Object>> enable() {
        return ApiResponse.ok(timeService.enable());
    }

    // (추가) OFF
    @PostMapping("/dnd/disable")
    public ApiResponse<Map<String, Object>> disable() {
        return ApiResponse.ok(timeService.disable());
    }

    // 3) 금지시간 보류 큐 상태 조회
    @GetMapping("/dnd/queue")
    public ApiResponse<Map<String, Object>> queueStatus() {
        return ApiResponse.ok(timeService.queueStatus());
    }

    // 4) 금지시간 해제 후 발송 플러시
    // - 보통 운영에서는 "disable + flush"를 한 번에 하고 싶어서 같이 묶어둠
    @PostMapping("/dnd/release-and-flush")
    public ApiResponse<Map<String, Object>> releaseAndFlush() {
        timeService.disable();
        return ApiResponse.ok(messageService.flushDndQueue());
    }

    // 4-b) 그냥 flush만(원하면)
    @PostMapping("/dnd/flush")
    public ApiResponse<Map<String, Object>> flushOnly() {
        // 아직 DND면 flush해도 다시 QUEUED 될 수 있으니 방지(권장)
        if (timeService.isDndNow()) {
            return ApiResponse.ok(Map.of(
                    "flushed", 0,
                    "queuedLeft", timeService.queueStatus().get("queuedCount"),
                    "message", "DND is still active. Disable DND before flushing."
            ));
        }
        return ApiResponse.ok(messageService.flushDndQueue());
    }

    // 5) (검증) 금지시간 중 발송 차단 여부 확인
    // - 지금 구조에서는 isDndNow()가 true면 sendWithRetry에서 QUEUED로 보내고 return -> "차단"이 맞음
    @GetMapping("/dnd/verify-block")
    public ApiResponse<Map<String, Object>> verifyBlock() {
        boolean dndNow = timeService.isDndNow();
        return ApiResponse.ok(Map.of(
                "dndNow", dndNow,
                "sendingBlocked", dndNow,
                "behavior", dndNow ? "Requests will be queued (status=QUEUED)." : "Requests can be sent."
        ));
    }
}