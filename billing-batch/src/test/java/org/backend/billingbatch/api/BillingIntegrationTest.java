package org.backend.billingbatch.api;

import org.backend.billingbatch.entity.BillingHistory;
import org.backend.billingbatch.entity.MicroPayment;
import org.backend.billingbatch.repository.BillingHistoryRepository;
import org.backend.billingbatch.repository.InvoiceRepository;
import org.backend.billingbatch.repository.MicroPaymentRepository;
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
    private MicroPaymentRepository microPaymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        invoiceRepository.deleteAll();
        billingHistoryRepository.deleteAll();
        microPaymentRepository.deleteAll();
    }

    @Test
    @DisplayName("통합: API로 배치를 실행하면 실제 DB에 청구서가 생성된다")
    void runBatchAndGetInvoicesTest() throws Exception {
        // Given
        String targetMonth = "2024-01";
        BillingHistory user1 = new BillingHistory(1L, BigDecimal.valueOf(50000), targetMonth);
        MicroPayment pay1 = new MicroPayment(1L, targetMonth, BigDecimal.valueOf(10000));
        BillingHistory user2 = new BillingHistory(2L, BigDecimal.valueOf(30000), targetMonth);

        billingHistoryRepository.saveAll(List.of(user1, user2));
        microPaymentRepository.save(pay1);

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