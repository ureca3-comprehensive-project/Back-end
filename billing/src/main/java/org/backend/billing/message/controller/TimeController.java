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

    @GetMapping("/users/{userId}/dnd")
    public ApiResponse<Map<String, Object>> getDnd(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(timeService.get(userId));
    }

    @PutMapping("/users/{userId}/dnd")
    public ApiResponse<Map<String, Object>> updateDnd(
            @PathVariable("userId") Long userId,
            @RequestBody TimeUpdateRequest req
    ) {
        return ApiResponse.ok(timeService.update(userId, req));
    }

    @PostMapping("/users/{userId}/dnd/enable")
    public ApiResponse<Map<String, Object>> enable(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(timeService.enable(userId));
    }

    @PostMapping("/users/{userId}/dnd/disable")
    public ApiResponse<Map<String, Object>> disable(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(timeService.disable(userId));
    }

    @GetMapping("/users/{userId}/dnd/queue")
    public ApiResponse<Map<String, Object>> queueStatus(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(timeService.queueStatus(userId));
    }

    @PostMapping("/users/{userId}/dnd/release-and-flush")
    public ApiResponse<Map<String, Object>> releaseAndFlush(@PathVariable("userId") Long userId) {
        timeService.disable(userId);
        return ApiResponse.ok(messageService.flushDndHold(userId));
    }

    @PostMapping("/users/{userId}/dnd/flush")
    public ApiResponse<Map<String, Object>> flushOnly(@PathVariable("userId") Long userId) {
        if (timeService.isDndNow(userId)) {
            return ApiResponse.ok(Map.of(
                    "flushed", 0,
                    "queuedLeft", timeService.queueStatus(userId).get("queuedCount"),
                    "message", "DND is still active. Disable DND before flushing."
            ));
        }
        return ApiResponse.ok(messageService.flushDndHold(userId));
    }

    @GetMapping("/users/{userId}/dnd/verify-block")
    public ApiResponse<Map<String, Object>> verifyBlock(@PathVariable("userId") Long userId) {
        boolean dndNow = timeService.isDndNow(userId);
        return ApiResponse.ok(Map.of(
                "userId", userId,
                "dndNow", dndNow,
                "sendingBlocked", dndNow,
                "behavior", dndNow ? "Requests will be held (status=DND_HOLD)." : "Requests can be sent."
        ));
    }
}
