package org.backend.billing.message.entity;

import jakarta.persistence.*;
import org.backend.billing.message.type.MessageType;
import org.backend.billing.message.type.MessageTypeConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "template")
public class TemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 DTO: name/channel/subjectTemplate/bodyTemplate/allowedVariables/version 사용 중이라 유지
    @Column(nullable = false, length = 100)
    private String name;

    @Convert(converter = MessageTypeConverter.class)
    @Column(name = "type", nullable = false)
    private MessageType type;

    @Column(name = "subject_template", length = 255)
    private String subjectTemplate;

    @Lob
    @Column(name = "body_template", nullable = false)
    private String bodyTemplate;

    // JSON 문자열로 저장: ["userName","amount"] 같은 형태
    @Lob
    @Column(name = "allowed_variables")
    private String allowedVariablesJson;

    @Column(nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TemplateEntity() {}

    public TemplateEntity(String name, MessageType type, String subjectTemplate, String bodyTemplate, String allowedVariablesJson) {
        this.name = name;
        this.type = type;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.allowedVariablesJson = allowedVariablesJson;
        this.version = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public MessageType getType() { return type; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public String getAllowedVariablesJson() { return allowedVariablesJson; }
    public int getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String subjectTemplate, String bodyTemplate, String allowedVariablesJson) {
        if (name != null) this.name = name;
        if (subjectTemplate != null) this.subjectTemplate = subjectTemplate;
        if (bodyTemplate != null) this.bodyTemplate = bodyTemplate;
        if (allowedVariablesJson != null) this.allowedVariablesJson = allowedVariablesJson;
        this.version += 1;
    }
}