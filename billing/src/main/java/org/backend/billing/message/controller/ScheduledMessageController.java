package org.backend.billing.message.controller;

import java.util.List;
import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages/scheduled")
public class ScheduledMessageController {

    private final MessageService messageService;

    public ScheduledMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> scheduledList() {
        return ApiResponse.ok(messageService.scheduledList());
    }

    @PostMapping("/dnd/flush")
    public ApiResponse<Map<String, Object>> flushDndQueue() {
        return ApiResponse.ok(messageService.flushDndQueue());
    }
}