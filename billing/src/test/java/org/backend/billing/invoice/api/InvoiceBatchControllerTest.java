package org.backend.billing.invoice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.backend.billing.invoice.controller.InvoiceBatchController;
import org.backend.core.dto.BatchRunRequest;
import org.backend.core.dto.BatchRunResponse;
import org.backend.core.port.InvoiceBatchPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvoiceBatchControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean // Spring Boot 3.4+ 기준 (이전 버전은 @MockBean 사용)
    private InvoiceBatchPort invoiceBatchPort;

    @Test
    @DisplayName("배치 요약 정보 조회 성공")
    void getBatchSummary_Success() throws Exception {
        // given
        Map<String, Object> summary = Map.of("jobName", "createInvoiceJob", "totalCount", 100);
        given(invoiceBatchPort.getBatchSummary("createInvoiceJob")).willReturn(summary);

        // when & then
        mockMvc.perform(get("/billing/invoices/1/summary")
                        .param("jobName", "createInvoiceJob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("createInvoiceJob"))
                .andExpect(jsonPath("$.totalCount").value(100));
    }

    @Test
    @DisplayName("배치 실패 에러 로그 조회 성공")
    void getBatchErrors_Success() throws Exception {
        // given
        List<String> errors = List.of("Connection Timeout", "Deadlock detected");
        given(invoiceBatchPort.getJobErrors(1L)).willReturn(errors);

        // when & then
        mockMvc.perform(get("/billing/invoices/1/errors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Connection Timeout"));
    }

    @Test
    @DisplayName("배치 중단(취소) 요청 성공")
    void stopBatch_Success() throws Exception {
        // given
        doNothing().when(invoiceBatchPort).stopJob(1L);

        // when & then
        mockMvc.perform(post("/billing/invoices/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("배치 실행 목록 조회(대시보드) 성공")
    void getBatchRuns_Success() throws Exception {
        // given
        BatchRunResponse response = BatchRunResponse.builder()
                .jobExecutionId(1L)
                .status("COMPLETED")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                // 필요한 필드만 넣거나, 빌더이므로 나머지는 null로 들어감
                .build();
        given(invoiceBatchPort.getJobExecutions(0, 10)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/billing/invoices")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobExecutionId").value(1L))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("수동 배치 트리거 성공")
    void runBatchManually_Success() throws Exception {
        // given
        BatchRunRequest request = BatchRunRequest.builder()
                .billingMonth("2025-12")
                .jobName("createInvoiceJob") // 필요한 필드만 지정 가능
                .build();
        given(invoiceBatchPort.runJobManually(any(BatchRunRequest.class))).willReturn(100L);

        // when & then
        mockMvc.perform(post("/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @DisplayName("수동 배치 트리거 시 이미 실행 중이면 409 응답")
    void runBatchManually_Conflict() throws Exception {
        // given
        BatchRunRequest request = BatchRunRequest.builder()
                .billingMonth("2025-12")
                .jobName("createInvoiceJob") // 필요한 필드만 지정 가능
                .build();
        given(invoiceBatchPort.runJobManually(any(BatchRunRequest.class))).willReturn(null);

        // when & then
        mockMvc.perform(post("/billing/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("청구서 중복 검증 보고서 조회 성공")
    void getDuplicateReport_Success() throws Exception {
        // given
        Map<String, Object> report = Map.of("billingMonth", "2025-12", "duplicateCount", 0);
        given(invoiceBatchPort.getDuplicateReport("2025-12")).willReturn(report);

        // when & then
        mockMvc.perform(get("/billing/invoices/validations/duplicates")
                        .param("billingMonth", "2025-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicateCount").value(0));
    }
}