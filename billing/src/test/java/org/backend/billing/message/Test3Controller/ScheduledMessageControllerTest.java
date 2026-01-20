package org.backend.billing.message.Test3Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.backend.billing.message.controller.ScheduledMessageController;
import org.backend.billing.message.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduledMessageController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 있으면 필터 비활성화
class ScheduledMessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MessageService messageService;

    @Test
    @DisplayName("GET /messages/scheduled - 예약 발송 목록 조회")
    void scheduledList() throws Exception {
        when(messageService.scheduledList())
                .thenReturn(List.of(Map.of("messageId", 1L, "status", "SCHEDULED")));

        mockMvc.perform(get("/messages/scheduled"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).scheduledList();
    }

    @Test
    @DisplayName("POST /messages/scheduled/dnd/flush - DND 큐 flush")
    void flushDndQueue() throws Exception {
        when(messageService.flushDndQueue())
                .thenReturn(Map.of("flushed", 3));

        mockMvc.perform(post("/messages/scheduled/dnd/flush"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(messageService).flushDndQueue();
    }
}