package org.backend.billing.message.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.backend.billing.common.exception.ApiException;
import org.backend.billing.common.exception.ErrorCode;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.entity.TemplateEntity;
import org.backend.billing.message.repository.TemplateRepository;
import org.backend.billing.message.type.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional
    public TemplateEntity create(TemplateCreateRequest req) {
        MessageType type = parseType(req.channel());
        String allowedJson = toJson(Optional.ofNullable(req.allowedVariables()).orElse(List.of()));

        TemplateEntity t = new TemplateEntity(
                req.name(),
                type,
                req.subjectTemplate(),
                req.bodyTemplate(),
                allowedJson
        );
        return templateRepository.save(t);
    }

    @Transactional
    public TemplateEntity update(TemplateUpdateRequest req) {
        TemplateEntity t = templateRepository.findById(req.templateId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "template not found: " + req.templateId()));

        String allowedJson = (req.allowedVariables() == null) ? null : toJson(req.allowedVariables());

        // ✅ UpdateRequest엔 channel이 없으므로 type은 유지
        t.update(req.name(), req.subjectTemplate(), req.bodyTemplate(), allowedJson);
        return t;
    }

    @Transactional
    public void delete(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "template not found: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }

    @Transactional(readOnly = true)
    public Map<String, String> preview(TemplatePreviewRequest req) {
        TemplateEntity t = templateRepository.findById(req.templateId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "template not found: " + req.templateId()));

        Map<String, String> vars = Optional.ofNullable(req.variables()).orElse(Map.of());
        List<String> allowed = fromJsonList(t.getAllowedVariablesJson());

        validateVariables(allowed, vars);

        String subject = t.getSubjectTemplate() == null ? null : apply(t.getSubjectTemplate(), vars);
        String body = apply(t.getBodyTemplate(), vars);

        return Map.of(
                "templateId", String.valueOf(t.getId()),
                "version", String.valueOf(t.getVersion()),
                "subject", subject == null ? "" : subject,
                "body", body
        );
    }

    private void validateVariables(List<String> allowed, Map<String, String> vars) {
        for (String k : vars.keySet()) {
            if (!allowed.contains(k)) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "not allowed variable: " + k);
            }
        }
        for (String k : allowed) {
            if (!vars.containsKey(k)) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "missing variable: " + k);
            }
        }
    }

    private String apply(String template, Map<String, String> vars) {
        String out = template;
        for (var e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    private MessageType parseType(String channel) {
        if (channel == null) throw new ApiException(ErrorCode.BAD_REQUEST, "channel is required");
        try {
            return MessageType.valueOf(channel.toUpperCase());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "invalid channel: " + channel);
        }
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { throw new ApiException(ErrorCode.INTERNAL_ERROR, "json serialize failed"); }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); }
        catch (Exception e) { return List.of(); }
    }
}