package org.backend.billingbatch.api;

import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.billing.repository.BillingHistoryRepository;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.line.entity.Line;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BillingIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BillingHistoryRepository billingHistoryRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        invoiceRepository.deleteAll();
        billingHistoryRepository.deleteAll();
    }

    @Test
    @DisplayName("통합: API로 배치를 실행하면 실제 DB에 청구서가 생성된다")
    void runBatchAndGetInvoicesTest() throws Exception {
        // Given
        String targetMonth = "2024-01";
        Line line1 = Line.builder().id(1L).build();
        Line line2 = Line.builder().id(2L).build();
        BillingHistory user1 = BillingHistory.builder()
                .line(line1)
                .amount(BigDecimal.valueOf(50000))
                .billingMonth(targetMonth)
                .benefitAmount(BigDecimal.ZERO)
                .usage(100)
                .userAt(java.time.LocalDateTime.now())
                .planId(1L)
                .build();
        BillingHistory user2 = BillingHistory.builder()
                .line(line2)
                .amount(BigDecimal.valueOf(30000))
                .billingMonth(targetMonth)
                .benefitAmount(BigDecimal.ZERO)
                .usage(50)
                .userAt(java.time.LocalDateTime.now())
                .planId(1L)
                .build();

        billingHistoryRepository.saveAll(List.of(user1, user2));

        // When
        String jsonRequest = "{\"jobName\":\"createInvoiceJob\", \"billingMonth\":\"2024-01\", \"isForced\":false}";
        mockMvc.perform(post("/billing/runs/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andDo(print());

        // Then
        mockMvc.perform(get("/billing/runs").param("offset", "0").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("COMPLETED"))); // 실제 배치가 성공해야 함

        mockMvc.perform(get("/billing/bills").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2))); // 실제 데이터 2건 확인
    }
}