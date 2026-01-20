package org.backend.billing.message.Test3Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.backend.billing.message.controller.TimeController;
import org.backend.billing.message.dto.request.TimeUpdateRequest;
import org.backend.billing.message.service.MessageService;
import org.backend.billing.message.service.TimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TimeController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 있으면 우선 필터 비활성화
class TimeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TimeService timeService;

    @MockitoBean
    MessageService messageService;

    // 1) GET /messages/time/dnd
    @Test
    @DisplayName("GET /messages/time/dnd - DND 설정 조회")
    void getDnd() throws Exception {
        when(timeService.get()).thenReturn(Map.of(
                "startTime", "22:00",
                "endTime", "08:00",
                "enabled", true
        ));

        mockMvc.perform(get("/messages/time/dnd"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).get();
        verifyNoInteractions(messageService);
    }

    // 2) PUT /messages/time/dnd
    @Test
    @DisplayName("PUT /messages/time/dnd - DND 설정 업데이트")
    void updateDnd() throws Exception {
        when(timeService.update(any(TimeUpdateRequest.class)))
                .thenReturn(Map.of("updated", true));

        String body = """
            { "startTime": "22:00", "endTime": "08:00", "enabled": true }
            """;

        mockMvc.perform(put("/messages/time/dnd")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).update(any(TimeUpdateRequest.class));
        verifyNoInteractions(messageService);
    }

    // 3) POST /messages/time/dnd/enable
    @Test
    @DisplayName("POST /messages/time/dnd/enable - DND ON")
    void enable() throws Exception {
        when(timeService.enable()).thenReturn(Map.of("enabled", true));

        mockMvc.perform(post("/messages/time/dnd/enable"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).enable();
        verifyNoInteractions(messageService);
    }

    // 4) POST /messages/time/dnd/disable
    @Test
    @DisplayName("POST /messages/time/dnd/disable - DND OFF")
    void disable() throws Exception {
        when(timeService.disable()).thenReturn(Map.of("enabled", false));

        mockMvc.perform(post("/messages/time/dnd/disable"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).disable();
        verifyNoInteractions(messageService);
    }

    // 5) GET /messages/time/dnd/queue
    @Test
    @DisplayName("GET /messages/time/dnd/queue - DND 큐 상태 조회")
    void queueStatus() throws Exception {
        when(timeService.queueStatus()).thenReturn(Map.of("queuedCount", 2));

        mockMvc.perform(get("/messages/time/dnd/queue"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).queueStatus();
        verifyNoInteractions(messageService);
    }

    // 6) POST /messages/time/dnd/release-and-flush
    @Test
    @DisplayName("POST /messages/time/dnd/release-and-flush - DND OFF 후 flush")
    void releaseAndFlush() throws Exception {
        when(timeService.disable()).thenReturn(Map.of("enabled", false));
        when(messageService.flushDndQueue()).thenReturn(Map.of("flushed", 3));

        mockMvc.perform(post("/messages/time/dnd/release-and-flush"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        // 순서까지 검증(disable 먼저, flush 나중)
        var inOrder = inOrder(timeService, messageService);
        inOrder.verify(timeService).disable();
        inOrder.verify(messageService).flushDndQueue();
    }

    // 7) POST /messages/time/dnd/flush - DND 활성일 때 (flush 막고 안내 메시지)
    @Test
    @DisplayName("POST /messages/time/dnd/flush - DND active면 flush 하지 않고 안내")
    void flushOnly_whenDndActive() throws Exception {
        when(timeService.isDndNow()).thenReturn(true);
        when(timeService.queueStatus()).thenReturn(Map.of("queuedCount", 5));

        mockMvc.perform(post("/messages/time/dnd/flush"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                // ApiResponse 구조가 data로 감싸져있다는 가정 하에 핵심 필드만 확인
                .andExpect(jsonPath("$.data.flushed").value(0))
                .andExpect(jsonPath("$.data.queuedLeft").value(5));

        verify(timeService).isDndNow();
        verify(timeService).queueStatus();
        verifyNoInteractions(messageService); // ✅ flush 호출되면 안 됨
    }

    // 8) POST /messages/time/dnd/flush - DND 비활성일 때 (flush 수행)
    @Test
    @DisplayName("POST /messages/time/dnd/flush - DND inactive면 flush 수행")
    void flushOnly_whenDndInactive() throws Exception {
        when(timeService.isDndNow()).thenReturn(false);
        when(messageService.flushDndQueue()).thenReturn(Map.of("flushed", 2));

        mockMvc.perform(post("/messages/time/dnd/flush"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));

        verify(timeService).isDndNow();
        verify(messageService).flushDndQueue();
        verify(timeService, never()).queueStatus();
    }

    // 9) GET /messages/time/dnd/verify-block
    @Test
    @DisplayName("GET /messages/time/dnd/verify-block - dndNow=true이면 sendingBlocked=true")
    void verifyBlock_whenDndNowTrue() throws Exception {
        when(timeService.isDndNow()).thenReturn(true);

        mockMvc.perform(get("/messages/time/dnd/verify-block"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.dndNow").value(true))
                .andExpect(jsonPath("$.data.sendingBlocked").value(true));

        verify(timeService).isDndNow();
        verifyNoInteractions(messageService);
    }

    @Test
    @DisplayName("GET /messages/time/dnd/verify-block - dndNow=false이면 sendingBlocked=false")
    void verifyBlock_whenDndNowFalse() throws Exception {
        when(timeService.isDndNow()).thenReturn(false);

        mockMvc.perform(get("/messages/time/dnd/verify-block"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.dndNow").value(false))
                .andExpect(jsonPath("$.data.sendingBlocked").value(false));

        verify(timeService).isDndNow();
        verifyNoInteractions(messageService);
    }
}