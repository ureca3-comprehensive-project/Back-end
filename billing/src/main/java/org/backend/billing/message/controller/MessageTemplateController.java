package org.backend.billing.message.controller;

import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.service.InMemoryStores.Template;
import org.backend.billing.message.service.TemplateService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/messages/template")
public class MessageTemplateController {

    private final TemplateService templateService;

    public MessageTemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    // 1) 등록
    @PostMapping
    public ApiResponse<Template> create(@RequestBody TemplateCreateRequest req) {
        return ApiResponse.ok(templateService.create(req));
    }

    // 2) 수정
    @PatchMapping("/{templateId}")
    public ApiResponse<Template> update(@PathVariable("templateId") Long templateId,
                                        @RequestBody TemplateUpdateRequest req) {
        TemplateUpdateRequest fixed = new TemplateUpdateRequest(
                templateId,
                req.name(),
                req.subjectTemplate(),
                req.bodyTemplate(),
                req.allowedVariables()
        );
        return ApiResponse.ok(templateService.update(fixed));
    }

    // 3) 삭제
    @DeleteMapping("/{templateId}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable("templateId") Long templateId) {
        templateService.delete(templateId);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    // 4) 미리보기 (POST 추천)
    @PostMapping("/preview")
    public ApiResponse<Map<String, String>> preview(@RequestBody TemplatePreviewRequest req) {
        return ApiResponse.ok(templateService.preview(req));
    }
}