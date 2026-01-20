package org.backend.billingbatch.api;

import org.backend.billingbatch.dto.BatchRunRequest;
import org.backend.billingbatch.dto.BatchRunResponse;
import org.backend.billingbatch.services.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BillingControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private BatchService batchService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("1. 정산 배치 실행 (스케줄러/수동) 테스트")
    void runBatchTest() throws Exception {
        // given
        BatchRunRequest request = new BatchRunRequest();
        given(batchService.runJob(any(BatchRunRequest.class))).willReturn(123L);

        // when & then
        mockMvc.perform(post("/billing/runs/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobName\":\"createInvoiceJob\", \"billingMonth\":\"2024-01\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("123"))
                .andDo(print());
    }

    @Test
    @DisplayName("2. 정산 배치 실행 목록 조회 (대시보드) 테스트")
    void getBatchRunsTest() throws Exception {
        // given
        BatchRunResponse response = BatchRunResponse.builder()
                .jobExecutionId(1L)
                .jobName("createInvoiceJob")
                .status("COMPLETED")
                .billingMonth("2024-01")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();

        given(batchService.getJobExecutions(anyInt(), anyInt()))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/billing/runs")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobName").value("createInvoiceJob"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andDo(print());
    }

    @Test
    @DisplayName("3. 정산 배치 실행 단건 조회 테스트")
    void getBatchRunDetailTest() throws Exception {
        // given
        Long executionId = 1L;
        BatchRunResponse response = BatchRunResponse.builder()
                .jobExecutionId(executionId)
                .status("FAILED")
                .build();

        given(batchService.getJobExecutionDetail(executionId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/billing/runs/{runId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andDo(print());
    }

    @Test
    @DisplayName("4. 정산 배치 요약 조회 테스트")
    void getBatchSummaryTest() throws Exception {
        // given
        Long executionId = 1L;
        String jobName = "createInvoiceJob";
        given(batchService.getBatchSummary(jobName))
                .willReturn(Map.of("totalInstanceCount", 10, "jobName", jobName));

        // when & then
        // 주의: 실제 로직가 다르게 {runId}말고 @RequestParam jobName을 사용해 테스트
        mockMvc.perform(get("/billing/runs/{runId}/summary", executionId)
                        .param("jobName", jobName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInstanceCount").value(10))
                .andDo(print());
    }

    @Test
    @DisplayName("5. 정산 배치 실패 상세 조회 테스트")
    void getBatchErrorsTest() throws Exception {
        // given
        Long executionId = 1L;
        given(batchService.getJobErrors(executionId))
                .willReturn(Arrays.asList("Error 1: Timeout", "Error 2: NullPointer"));

        // when & then
        mockMvc.perform(get("/billing/runs/{runId}/errors", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Error 1: Timeout"))
                .andDo(print());
    }

    @Test
    @DisplayName("6. 정산 배치 재처리 테스트")
    void retryBatchTest() throws Exception {
        // given
        Long failedExecutionId = 1L;
        Long newExecutionId = 2L;
        given(batchService.retryJob(failedExecutionId)).willReturn(newExecutionId);

        // when & then
        mockMvc.perform(get("/billing/runs/{runId}/retry", failedExecutionId))
                .andExpect(status().isOk())
                .andExpect(content().string("2"))
                .andDo(print());
    }

    @Test
    @DisplayName("7. 정산 배치 취소 테스트")
    void stopBatchTest() throws Exception {
        // given
        Long executionId = 1L;
        doNothing().when(batchService).stopJob(executionId);

        // when & then
        mockMvc.perform(post("/billing/runs/{runId}/cancel", executionId))
                .andExpect(status().isOk())
                .andDo(print());

        // 서비스 메서드가 진짜 호출됐는지 검증
        verify(batchService).stopJob(executionId);
    }
}
