//package org.backend.billing.invoice.api;
//
//import org.backend.core.dto.LockStatusResponse;
//import org.backend.billing.invoice.services.BatchService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class LockTest {
//    private MockMvc mockMvc;
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @MockitoBean
//    private BatchService batchService;
//
//    @MockitoBean
//    private LockService lockService;
//
//    @BeforeEach
//    void setUp() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//    }
//
//    @Test
//    @DisplayName("강제 잠금 해제(Unlock) API 호출 시 서비스가 실행된다")
//    void unlockTest() throws Exception {
//        // when
//        mockMvc.perform(delete("/billing/locks"))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        // then
//        // 실제 서비스의 forceUnlock() 메서드가 호출되었는지 검증
//        verify(lockService).forceUnlock();
//    }
//
//    @Test
//    @DisplayName("락 상태 조회(GetStatus) API가 정상 응답한다")
//    void getLockStatusTest() throws Exception {
//        // given
//        LockStatusResponse mockResponse = LockStatusResponse.builder()
//                .locked(true)
//                .runningCount(1)
//                .statusMessage("BATCH_RUNNING")
//                .build();
//
//        given(lockService.getLockStatus()).willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/billing/locks")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.locked").value(true))
//                .andExpect(jsonPath("$.runningCount").value(1)) // 실행 중인 개수 검증
//                .andExpect(jsonPath("$.statusMessage").value("BATCH_RUNNING")); // 메시지 검증
//    }
//
//    @Test
//    @DisplayName("중복 리포트 조회 API가 정상 응답한다")
//    void getDuplicateReportTest() throws Exception {
//        // given
//        String month = "2024-01";
//        Map<String, Object> mockReport = new HashMap<>();
//        mockReport.put("totalDuplicates", 5);
//        mockReport.put("message", "중복 데이터 확인됨");
//
//        given(batchService.getDuplicateReport(month)).willReturn(mockReport);
//
//        // when & then
//        mockMvc.perform(get("/billing/runs/validations/duplicates")
//                        .param("billingMonth", month)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.totalDuplicates").value(5))
//                .andExpect(jsonPath("$.message").value("중복 데이터 확인됨"));
//    }
//}
