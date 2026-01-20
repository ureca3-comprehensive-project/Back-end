package org.backend.billing.message.service;
/*
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.backend.billing.common.exception.ApiException;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateServiceTest {

    private InMemoryStores stores;
    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        stores = new InMemoryStores();
        templateService = new TemplateService(stores);
    }

    @Test
    void create_shouldCreateTemplate() {
        var req = new TemplateCreateRequest(
            "welcome",
            "EMAIL",
            "Hello {userName}",
            "Body {userName} {amount}",
            List.of("userName", "amount")
        );

        var created = templateService.create(req);

        assertNotNull(created);
        assertEquals("welcome", created.name());
        assertEquals(InMemoryStores.Channel.EMAIL, created.channel());
        assertEquals(1, created.version());
        assertTrue(stores.templates.containsKey(created.id()));
    }

    @Test
    void update_shouldIncreaseVersion() {
        var created = templateService.create(new TemplateCreateRequest(
            "welcome", "EMAIL", "S", "B", List.of("userName")
        ));

        var updated = templateService.update(new TemplateUpdateRequest(
            created.id(),
            "welcome-v2",
            null,
            "Body {userName}",
            List.of("userName")
        ));

        assertEquals(created.id(), updated.id());
        assertEquals("welcome-v2", updated.name());
        assertEquals(2, updated.version());
    }

    @Test
    void preview_shouldApplyVariables() {
        var created = templateService.create(new TemplateCreateRequest(
            "welcome", "EMAIL", "Hi {userName}", "Pay {amount}", List.of("userName", "amount")
        ));

        var res = templateService.preview(new TemplatePreviewRequest(
            created.id(),
            Map.of("userName", "Minseok", "amount", "1000")
        ));

        assertEquals("Hi Minseok", res.get("subject"));
        assertEquals("Pay 1000", res.get("body"));
    }

    @Test
    void preview_shouldFailWhenMissingVariable() {
        var created = templateService.create(new TemplateCreateRequest(
            "welcome", "EMAIL", "Hi {userName}", "Pay {amount}", List.of("userName", "amount")
        ));

        assertThrows(ApiException.class, () ->
            templateService.preview(new TemplatePreviewRequest(
                created.id(),
                Map.of("userName", "Minseok") // amount 누락
            ))
        );
    }
}
*/