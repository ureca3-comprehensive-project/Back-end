package org.backend.billing.message.controller;

import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.entity.TemplateEntity;
import org.backend.billing.message.service.TemplateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages/template")
public class MessageTemplateController {

    private final TemplateService templateService;

    public MessageTemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ApiResponse<TemplateEntity> create(@RequestBody TemplateCreateRequest req) {
        return ApiResponse.ok(templateService.create(req));
    }

    @PatchMapping("/{templateId}")
    public ApiResponse<TemplateEntity> update(@PathVariable("templateId") Long templateId,
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

    @DeleteMapping("/{templateId}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable("templateId") Long templateId) {
        templateService.delete(templateId);
        return ApiResponse.ok(Map.of("deleted", true));
    }

    @PostMapping("/preview")
    public ApiResponse<Map<String, String>> preview(@RequestBody TemplatePreviewRequest req) {
        return ApiResponse.ok(templateService.preview(req));
    }
}