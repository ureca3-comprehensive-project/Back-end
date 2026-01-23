package org.backend.billing.message.Test3Controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.backend.billing.message.controller.TimeController;
import org.backend.billing.message.dto.request.TimeUpdateRequest;
import org.backend.billing.message.service.MessageService;
import org.backend.billing.message.service.TimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TimeController.class)
@AutoConfigureMockMvc(addFilters = false) // 혹시 보안필터 있으면 영향 제거
class TimeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TimeService timeService;
    @MockitoBean MessageService messageService;

    @Test
    void getDnd_ok() throws Exception {
        given(timeService.get(1L)).willReturn(Map.of(
                "userId", 1L,
                "enabled", true,
                "startTime", "22:00",
                "endTime", "08:00",
                "updatedAt", "2026-01-22T00:00:00",
                "dndNow", false
        ));

        mockMvc.perform(get("/messages/time/users/1/dnd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateDnd_ok() throws Exception {
        given(timeService.update(eq(1L), any(TimeUpdateRequest.class))).willReturn(Map.of(
                "userId", 1L,
                "enabled", true,
                "startTime", "23:00",
                "endTime", "07:00",
                "updatedAt", "2026-01-22T00:00:00",
                "dndNow", false
        ));

        String body = """
                {"startTime":"23:00","endTime":"07:00","enabled":true}
                """;

        mockMvc.perform(put("/messages/time/users/1/dnd")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void enable_ok() throws Exception {
        given(timeService.enable(1L)).willReturn(Map.of(
                "userId", 1L, "enabled", true, "updatedAt", "2026-01-22T00:00:00"
        ));

        mockMvc.perform(post("/messages/time/users/1/dnd/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void disable_ok() throws Exception {
        // ✅ 여기 반드시 stub: null 리턴이면 ApiResponse.ok에서 500 나는 케이스 많음
        given(timeService.disable(1L)).willReturn(Map.of(
                "userId", 1L, "enabled", false, "updatedAt", "2026-01-22T00:00:00"
        ));

        mockMvc.perform(post("/messages/time/users/1/dnd/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void queueStatus_ok() throws Exception {
        given(timeService.queueStatus(1L)).willReturn(Map.of(
                "userId", 1L, "queuedCount", 2
        ));

        mockMvc.perform(get("/messages/time/users/1/dnd/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void releaseAndFlush_ok() throws Exception {
        // 컨트롤러에서 disable을 먼저 호출하니까 이것도 stub 필요
        given(timeService.disable(1L)).willReturn(Map.of(
                "userId", 1L, "enabled", false, "updatedAt", "2026-01-22T00:00:00"
        ));
        given(messageService.flushDndHold(1L)).willReturn(Map.of(
                "userId", 1L, "flushed", 3, "queuedLeft", 0
        ));

        mockMvc.perform(post("/messages/time/users/1/dnd/release-and-flush"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flushed").value(3));
    }

    @Test
    void flushOnly_when_dnd_active_returns_message() throws Exception {
        given(timeService.isDndNow(1L)).willReturn(true);
        given(timeService.queueStatus(1L)).willReturn(Map.of(
                "userId", 1L, "queuedCount", 5
        ));

        mockMvc.perform(post("/messages/time/users/1/dnd/flush"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flushed").value(0));
    }
}
