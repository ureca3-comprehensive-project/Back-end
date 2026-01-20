package org.backend.billing.message.controller;
import java.util.List;
import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.dto.request.MessageSendRequest;
import org.backend.billing.message.dto.request.ResendRequest;
import org.backend.billing.message.dto.request.SendEmailRequest;
import org.backend.billing.message.dto.request.SendPhoneRequest;
import org.backend.billing.message.service.MessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // ===== Mock Send =====

    @PostMapping("/mock/sms")
    public ApiResponse<Map<String, Object>> sendSmsMock(@RequestBody SendPhoneRequest req) {
        return ApiResponse.ok(messageService.sendSmsMock(req));
    }

    @PostMapping("/mock/email")
    public ApiResponse<Map<String, Object>> sendEmailMock(@RequestBody SendEmailRequest req) {
        return ApiResponse.ok(messageService.sendEmailMock(req));
    }

    // ===== Main Send =====

    // 즉시/예약 발송 생성
    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> createSend(@RequestBody MessageSendRequest req) {
        return ApiResponse.ok(messageService.createSend(req));
    }

    // 명세에 있는 /messages/send/{type} 대응
    @PostMapping("/send/{type}")
    public ApiResponse<Map<String, Object>> sendByType(
            @PathVariable("type") String type,
            @RequestBody MessageSendRequest req
    ) {
        return ApiResponse.ok(messageService.sendByType(type, req));
    }

    // 이메일 1% 실패 테스트
    @PostMapping("/error")
    public ApiResponse<Map<String, Object>> emailErrorTest(@RequestBody SendEmailRequest req) {
        return ApiResponse.ok(messageService.emailErrorTest(req));
    }

    // ===== Query =====

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(messageService.list());
    }

    @GetMapping("/{messageId}")
    public ApiResponse<Map<String, Object>> getMessage(
            @PathVariable("messageId") Long messageId
    ) {
        return ApiResponse.ok(messageService.getMessage(messageId));
    }

    // 사용자 상태 조회(명세 충돌 회피용)
    @GetMapping("/users/{userId}/status")
    public ApiResponse<Map<String, Object>> getUserStatus(
            @PathVariable("userId") Long userId
    ) {
        return ApiResponse.ok(messageService.getUserStatus(userId));
    }

    // 취소
    @PatchMapping("/{messageId}/cancel")
    public ApiResponse<Map<String, Object>> cancel(
            @PathVariable("messageId") Long messageId
    ) {
        return ApiResponse.ok(messageService.cancel(messageId));
    }

    // 실패 목록
    @GetMapping("/failures")
    public ApiResponse<List<Map<String, Object>>> failures() {
        return ApiResponse.ok(messageService.failures());
    }

    // attempt 히스토리
    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> history() {
        return ApiResponse.ok(messageService.history());
    }

    // 이메일 실패시 SMS 대체 발송
    @PostMapping("/resend")
    public ApiResponse<Map<String, Object>> resend(@RequestBody ResendRequest req) {
        return ApiResponse.ok(messageService.resend(req));
    }
}