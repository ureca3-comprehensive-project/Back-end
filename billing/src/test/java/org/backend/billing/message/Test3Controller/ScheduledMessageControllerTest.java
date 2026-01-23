package org.backend.billing.message.Test3Controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.backend.billing.message.controller.ScheduledMessageController;
import org.backend.billing.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduledMessageController.class)
class ScheduledMessageControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean MessageService messageService;

    @Test
    void scheduledList_ok() throws Exception {
        given(messageService.scheduledList()).willReturn(List.of(
                Map.of("id", 1, "status", "PENDING")
        ));

        mockMvc.perform(get("/messages/scheduled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void flushDndQueue_ok() throws Exception {
        // 네 프로젝트에서 메서드명이 flushDndQueue면 이대로 OK
        given(messageService.flushDndQueue()).willReturn(Map.of("flushed", 3, "queuedLeft", 0));

        mockMvc.perform(post("/messages/scheduled/dnd/flush"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flushed").value(3));
    }
}