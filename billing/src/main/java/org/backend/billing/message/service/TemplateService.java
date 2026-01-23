package org.backend.billing.message.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.backend.billing.common.exception.ApiException;
import org.backend.billing.common.exception.ErrorCode;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.service.InMemoryStores.Channel;
import org.backend.billing.message.service.InMemoryStores.Template;
import org.springframework.stereotype.Service;


@Service
public class TemplateService {

    private final InMemoryStores stores;

    public TemplateService(InMemoryStores stores) {
        this.stores = stores;
    }

    public Template create(TemplateCreateRequest req) {
        Channel ch = parseChannel(req.channel());
        Long id = stores.templateSeq.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();

        Template t = new Template(
                id,
                req.name(),
                ch,
                req.subjectTemplate(),
                req.bodyTemplate(),
                Optional.ofNullable(req.allowedVariables()).orElse(List.of()),
                1,
                now,
                now
        );
        stores.templates.put(id, t);
        return t;
    }

    public Template update(TemplateUpdateRequest req) {
        Template old = stores.templates.get(req.templateId());
        if (old == null) throw new ApiException(ErrorCode.NOT_FOUND, "template not found: " + req.templateId());

        Template updated = new Template(
                old.id(),
                req.name() != null ? req.name() : old.name(),
                old.channel(),
                req.subjectTemplate() != null ? req.subjectTemplate() : old.subjectTemplate(),
                req.bodyTemplate() != null ? req.bodyTemplate() : old.bodyTemplate(),
                req.allowedVariables() != null ? req.allowedVariables() : old.allowedVariables(),
                old.version() + 1,
                old.createdAt(),
                LocalDateTime.now()
        );
        stores.templates.put(updated.id(), updated);
        return updated;
    }

    public void delete(Long templateId) {
        if (stores.templates.remove(templateId) == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "template not found: " + templateId);
        }
    }

    public Map<String, String> preview(TemplatePreviewRequest req) {
        Template t = stores.templates.get(req.templateId());
        if (t == null) throw new ApiException(ErrorCode.NOT_FOUND, "template not found: " + req.templateId());

        Map<String, String> vars = Optional.ofNullable(req.variables()).orElse(Map.of());
        validateVariables(t.allowedVariables(), vars);

        String subject = t.subjectTemplate() == null ? null : apply(t.subjectTemplate(), vars);
        String body = apply(t.bodyTemplate(), vars);

        return Map.of(
                "templateId", String.valueOf(t.id()),
                "version", String.valueOf(t.version()),
                "subject", subject == null ? "" : subject,
                "body", body
        );
    }

    private void validateVariables(List<String> allowed, Map<String, String> vars) {
        // 허용 변수 외 들어오면 실패
        for (String k : vars.keySet()) {
            if (!allowed.contains(k)) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "not allowed variable: " + k);
            }
        }
        // 누락 변수 체크(템플릿에 {x}가 있는 경우)
        // 간단히: allowed 변수가 있는데 값이 없으면 실패로 처리(요구사항 반영)
        for (String k : allowed) {
            if (!vars.containsKey(k)) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "missing variable: " + k);
            }
        }
    }

    private String apply(String template, Map<String, String> vars) {
        String out = template;
        for (var e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }

    private Channel parseChannel(String s) {
        try {
            return Channel.valueOf(s.toUpperCase());
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "invalid channel: " + s);
        }
    }
}