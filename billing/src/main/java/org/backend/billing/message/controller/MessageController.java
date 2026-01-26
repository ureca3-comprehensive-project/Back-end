package org.backend.billing.message.controller;

import java.util.List;
import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {
	
	private final MessageService messageService;
	
	// ✅ 프론트 3번: 목록
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestParam(name = "q", required = false) String q
    ) {
        return ApiResponse.ok(messageService.list(status, channel, q));
    }
    
 // ✅ 프론트 4번: 상세
    @GetMapping("/{messageId}")
    public ApiResponse<Map<String, Object>> getMessage(
            @PathVariable("messageId") Long messageId
    ) {
        return ApiResponse.ok(messageService.getMessage(messageId));
    }

}
