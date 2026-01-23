package org.backend.billing.message.Test3Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.backend.billing.message.controller.MessageTemplateController;
import org.backend.billing.message.dto.request.TemplateCreateRequest;
import org.backend.billing.message.dto.request.TemplatePreviewRequest;
import org.backend.billing.message.dto.request.TemplateUpdateRequest;
import org.backend.billing.message.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageTemplateController.class)
class MessageTemplateControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TemplateService templateService;

    @Test
    void create_ok() throws Exception {
        // 리턴 타입(TemplateResponse/Entity 등) 확실치 않아서 null 반환으로 컴파일 안정
        given(templateService.create(any(TemplateCreateRequest.class))).willReturn(null);

        String body = """
                {
                  "name":"welcome",
                  "channel":"EMAIL",
                  "subjectTemplate":"hi {userName}",
                  "bodyTemplate":"body {userName}",
                  "allowedVariables":["userName"]
                }
                """;

        mockMvc.perform(post("/messages/template")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void update_ok() throws Exception {
        given(templateService.update(any(TemplateUpdateRequest.class))).willReturn(null);

        String body = """
                {"name":"welcome2","subjectTemplate":"s","bodyTemplate":"b","allowedVariables":["x"]}
                """;

        mockMvc.perform(patch("/messages/template/10")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_ok() throws Exception {
        willDoNothing().given(templateService).delete(10L);

        mockMvc.perform(delete("/messages/template/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void preview_ok() throws Exception {
        given(templateService.preview(any(TemplatePreviewRequest.class)))
                .willReturn(Map.of("templateId", "10", "subject", "hi", "body", "hello"));

        String body = """
                {"templateId":10,"variables":{"userName":"민석"}}
                """;

        mockMvc.perform(post("/messages/template/preview")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.body").value("hello"));
    }
}
