package org.backend.billingbatch.api;

import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.Line;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InvoiceControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private List<Invoice> savedInvoices;

    @BeforeEach
    void setUp() {
        // MockMvc 수동 설정 (한글 깨짐 방지 필터 적용)
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        invoiceRepository.deleteAll();
        savedInvoices = new ArrayList<>();

        // 유저 A (ID: 100) 데이터 3건
        savedInvoices.add(createInvoice(100L, "2024-01", 15000));
        savedInvoices.add(createInvoice(100L, "2024-02", 14000));
        savedInvoices.add(createInvoice(100L, "2024-03", 16000));

        // 유저 B (ID: 200) 데이터 2건
        savedInvoices.add(createInvoice(200L, "2024-01", 25000));
        savedInvoices.add(createInvoice(200L, "2024-02", 22000));

        savedInvoices = invoiceRepository.saveAll(savedInvoices);
        invoiceRepository.flush();
        this.savedInvoices = invoiceRepository.findAll();
    }

    private Invoice createInvoice(Long lineId, String month, int amount) {
        Line line = Line.builder().id(lineId).build();
        BillingHistory bh = BillingHistory.builder().id(1L).build(); // 테스트용 임시 ID

        return Invoice.builder()
                .line(line)
                .billingHistory(bh)
                .billingMonth(month)
                .totalAmount(BigDecimal.valueOf(amount))
                .status(InvoiceStatus.CREATED)
                .build();
    }

    // 1. 전체 목록 조회 (페이징)
    @Test
    @DisplayName("청구서 목록 조회 - Page 객체 검증")
    void getInvoicesPagingTest() throws Exception {
        mockMvc.perform(get("/billing/bills")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // [수정] Page 객체는 $.content 안에 데이터가 있습니다.
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].lineId", is(100)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    // 2. 단건 조회
    @Test
    @DisplayName("청구서 ID로 단건 조회 성공")
    void getInvoiceByIdTest() throws Exception {
        Invoice target = savedInvoices.get(0);

        mockMvc.perform(get("/billing/bills/{billId}", target.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineId", is(target.getLine().getId().intValue())))
                .andExpect(jsonPath("$.billingMonth", is(target.getBillingMonth())));
    }

    // 3. 존재하지 않는 ID 조회
    @Test
    @DisplayName("존재하지 않는 청구서 조회 시 예외 발생 확인")
    void getInvoiceNotFoundTest() throws Exception {
        // GlobalExceptionHandler 없어도 되는 버전
//        assertThatThrownBy(() -> {
//            mockMvc.perform(get("/billing/bills/{billId}", 99999L));
//        }).isInstanceOf(ServletException.class) // MockMvc가 예외를 ServletException으로 한 번 감쌉니다.
//                .hasCauseInstanceOf(IllegalArgumentException.class) // 실제 원인은 IllegalArgumentException
//                .hasMessageContaining("청구서가 존재하지 않습니다. id=99999");


        // Given: 기대하는 상태와 메시지 정의
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND; // 404
        String expectedMessage = "청구서가 존재하지 않습니다. id=99999";

        mockMvc.perform(get("/billing/bills/{billId}", 99999L))
                .andExpect(status().is(expectedStatus.value())) // 실제 응답 코드가 404인지 확인
                .andExpect(jsonPath("$.status").value(expectedStatus.value())) // JSON 내부 값 확인
                .andExpect(jsonPath("$.message").value(expectedMessage)) // 내가 원하는 메시지 확인
                .andDo(print());
    }

    // 4. 유니크 키(회선+월) 조회
    @Test
    @DisplayName("회선번호와 청구월로 조회")
    void getInvoiceByKeyTest() throws Exception {
        mockMvc.perform(get("/billing/bills/byKey")
                        .param("lineId", "200")
                        .param("billingMonth", "2024-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineId", is(200)))
                .andExpect(jsonPath("$.billingMonth", is("2024-02")))
                .andExpect(jsonPath("$.totalAmount", is(22000)));
    }

    // 5. 상세 항목 조회
    @Test
    @DisplayName("청구서 상세 항목 조회 (빈 리스트)")
    void getInvoiceDetailsTest() throws Exception {
        Invoice target = savedInvoices.get(0);

        mockMvc.perform(get("/billing/bills/{billId}/items", target.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // 6. 단건 삭제
    @Test
    @DisplayName("청구서 ID로 삭제")
    void deleteInvoiceByIdTest() throws Exception {
        Invoice target = savedInvoices.get(0);
        long initialCount = invoiceRepository.count();

        mockMvc.perform(delete("/billing/bills/{billId}", target.getId()))
                .andExpect(status().isNoContent());

        assertEquals(initialCount - 1, invoiceRepository.count());
    }

    // 7. 월별 일괄 삭제
    @Test
    @DisplayName("특정 월 청구서 일괄 삭제")
    void deleteInvoicesByMonthTest() throws Exception {
        long initialCount = invoiceRepository.count();
        // 2024-01 데이터는 2건 (유저A, 유저B)
        mockMvc.perform(delete("/billing/bills")
                        .param("billingMonth", "2024-01"))
                .andExpect(status().isNoContent());

        assertEquals(initialCount - 2, invoiceRepository.count());
    }
}