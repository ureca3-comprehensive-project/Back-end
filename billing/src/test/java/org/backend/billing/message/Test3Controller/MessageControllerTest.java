package org.backend.billing.message.Test3Controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.backend.billing.message.controller.MessageController;
import org.backend.billing.message.dto.request.MessageSendRequest;
import org.backend.billing.message.dto.request.ResendRequest;
import org.backend.billing.message.dto.request.SendEmailRequest;
import org.backend.billing.message.dto.request.SendPhoneRequest;
import org.backend.billing.message.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false) // (Security 있으면 필터 비활성화)
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MessageService messageService;

    @Autowired
    ObjectMapper objectMapper;

    // ---------- Mock Send ----------

    @Test
    @DisplayName("POST /messages/mock/sms")
    void sendSmsMock() throws Exception {
        when(messageService.sendSmsMock(any(SendPhoneRequest.class)))
                .thenReturn(Map.of("result", "ok"));

        mockMvc.perform(post("/messages/mock/sms")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).sendSmsMock(any(SendPhoneRequest.class));
    }

    @Test
    @DisplayName("POST /messages/mock/email")
    void sendEmailMock() throws Exception {
        when(messageService.sendEmailMock(any(SendEmailRequest.class)))
                .thenReturn(Map.of("result", "ok"));

        SendEmailRequest req = new SendEmailRequest(
                "test@test.com",
                "hello",
                "content",
                "cid-001"
        );

        mockMvc.perform(post("/messages/mock/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(MockMvcResultHandlers.print()) // ✅ 실패하면 콘솔에 원인 출력
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).sendEmailMock(any(SendEmailRequest.class));
    }

    // ---------- Main Send ----------

    @Test
    @DisplayName("POST /messages/send")
    void createSend() throws Exception {
        when(messageService.createSend(any(MessageSendRequest.class)))
                .thenReturn(Map.of("messageId", 1L));

        mockMvc.perform(post("/messages/send")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).createSend(any(MessageSendRequest.class));
    }

    @Test
    @DisplayName("POST /messages/send/{type}")
    void sendByType() throws Exception {
        // ✅ stub 매칭 넓혀서(실패/500 방지)
        when(messageService.sendByType(anyString(), any(MessageSendRequest.class)))
                .thenReturn(Map.of("type", "sms", "result", "ok"));

        mockMvc.perform(post("/messages/send/{type}", "sms")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andDo(MockMvcResultHandlers.print()) // ✅ 실패하면 콘솔에 원인 출력
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).sendByType(eq("sms"), any(MessageSendRequest.class));
    }

    @Test
    @DisplayName("POST /messages/error")
    void emailErrorTest() throws Exception {
        when(messageService.emailErrorTest(any(SendEmailRequest.class)))
                .thenReturn(Map.of("errorTest", true));

        SendEmailRequest req = new SendEmailRequest(
                "test@test.com",
                "subject",
                "content",
                "cid-err-001"
        );

        mockMvc.perform(post("/messages/error")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).emailErrorTest(any(SendEmailRequest.class));
    }

    // ---------- Query ----------

    @Test
    @DisplayName("GET /messages")
    void list() throws Exception {
        when(messageService.list())
                .thenReturn(List.of(Map.of("messageId", 1L)));

        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).list();
    }

    @Test
    @DisplayName("GET /messages/{messageId}")
    void getMessage() throws Exception {
        when(messageService.getMessage(1L))
                .thenReturn(Map.of("messageId", 1L));

        mockMvc.perform(get("/messages/{messageId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).getMessage(1L);
    }

    @Test
    @DisplayName("GET /messages/users/{userId}/status")
    void getUserStatus() throws Exception {
        when(messageService.getUserStatus(10L))
                .thenReturn(Map.of("userId", 10L, "status", "ACTIVE"));

        mockMvc.perform(get("/messages/users/{userId}/status", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).getUserStatus(10L);
    }

    @Test
    @DisplayName("PATCH /messages/{messageId}/cancel")
    void cancel() throws Exception {
        // ✅ stub 매칭 넓혀서(실패/500 방지)
        when(messageService.cancel(anyLong()))
                .thenReturn(Map.of("messageId", 1L, "canceled", true));

        mockMvc.perform(patch("/messages/{messageId}/cancel", 1L))
                .andDo(MockMvcResultHandlers.print()) // ✅ 실패하면 콘솔에 원인 출력
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).cancel(1L);
    }

    @Test
    @DisplayName("GET /messages/failures")
    void failures() throws Exception {
        when(messageService.failures())
                .thenReturn(List.of(Map.of("messageId", 1L, "status", "FAIL")));

        mockMvc.perform(get("/messages/failures"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).failures();
    }

    @Test
    @DisplayName("GET /messages/history")
    void history() throws Exception {
        when(messageService.history())
                .thenReturn(List.of(Map.of("attemptId", 1L)));

        mockMvc.perform(get("/messages/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).history();
    }

    @Test
    @DisplayName("POST /messages/resend")
    void resend() throws Exception {
        when(messageService.resend(any(ResendRequest.class)))
                .thenReturn(Map.of("resend", true));

        ResendRequest req = new ResendRequest(1L, "01012341234");

        mockMvc.perform(post("/messages/resend")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(MockMvcResultHandlers.print()) // ✅ 실패하면 콘솔에 원인 출력
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).resend(any(ResendRequest.class));
    }
}