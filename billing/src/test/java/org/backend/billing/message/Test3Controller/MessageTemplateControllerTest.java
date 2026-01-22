package org.backend.billing.message.Test3Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.backend.billing.message.controller.MessageTemplateController;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.service.TemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemplateService templateService;

    @Test
    @DisplayName("POST /messages/template - 템플릿 등록이 정상 호출된다")
    void create_ok() throws Exception {
        when(templateService.create(any(TemplateCreateRequest.class))).thenReturn(null);

        String body = """
            {
              "name": "welcome",
              "subjectTemplate": "Hello {userName}",
              "bodyTemplate": "Amount: {amount}",
              "allowedVariables": ["userName","amount"]
            }
            """;

        mockMvc.perform(post("/messages/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(templateService, times(1)).create(any(TemplateCreateRequest.class));
    }

    @Test
    @DisplayName("PATCH /messages/template/{templateId} - PathVariable templateId가 req에 주입되어 update로 전달된다")
    void update_injects_path_id() throws Exception {
        when(templateService.update(any(TemplateUpdateRequest.class))).thenReturn(null);

        long pathId = 10L;

        String body = """
            {
              "templateId": 999,
              "name": "welcome2",
              "subjectTemplate": "Hi {userName}",
              "bodyTemplate": "Pay: {amount}",
              "allowedVariables": ["userName","amount"]
            }
            """;

        mockMvc.perform(patch("/messages/template/{templateId}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<TemplateUpdateRequest> captor = ArgumentCaptor.forClass(TemplateUpdateRequest.class);
        verify(templateService, times(1)).update(captor.capture());

        TemplateUpdateRequest fixed = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(pathId, fixed.templateId());
        org.junit.jupiter.api.Assertions.assertEquals("welcome2", fixed.name());
        org.junit.jupiter.api.Assertions.assertEquals("Hi {userName}", fixed.subjectTemplate());
        org.junit.jupiter.api.Assertions.assertEquals("Pay: {amount}", fixed.bodyTemplate());
        org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of("userName", "amount"), fixed.allowedVariables());
    }

    @Test
    @DisplayName("DELETE /messages/template/{templateId} - 삭제가 정상 호출된다")
    void delete_ok() throws Exception {
        doNothing().when(templateService).delete(5L);

        mockMvc.perform(delete("/messages/template/{templateId}", 5L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(templateService, times(1)).delete(5L);
    }

    @Test
    @DisplayName("POST /messages/template/preview - 미리보기가 정상 호출된다")
    void preview_ok() throws Exception {
        when(templateService.preview(any(TemplatePreviewRequest.class)))
                .thenReturn(Map.of("subject", "Hello Minseok", "body", "Amount: 1000"));

        String body = """
            {
              "templateId": 1,
              "variables": {
                "userName": "Minseok",
                "amount": "1000"
              }
            }
            """;

        mockMvc.perform(post("/messages/template/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(templateService, times(1)).preview(any(TemplatePreviewRequest.class));
    }
}
