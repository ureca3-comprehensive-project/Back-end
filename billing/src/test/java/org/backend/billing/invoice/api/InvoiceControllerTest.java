package org.backend.billing.invoice.api;

import jakarta.persistence.EntityManager;
import org.backend.core.port.InvoiceBatchPort;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.billing.repository.BillingHistoryRepository;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.invoice.type.InvoiceStatus;
import org.backend.domain.line.entity.DueDate;
import org.backend.domain.line.entity.Line;
import org.backend.domain.line.repository.LineRepository;
import org.backend.domain.line.type.LineStatus;
import org.backend.domain.user.entity.User;
import org.backend.domain.user.type.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @MockitoBean
    private InvoiceBatchPort invoiceBatchPort;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private BillingHistoryRepository billingHistoryRepository;

    @Autowired
    private EntityManager em;

    private List<Invoice> savedInvoices;

    private Line line100;
    private Line line200;

    @BeforeEach
    void setUp() {
        // MockMvc 수동 설정 (한글 깨짐 방지 필터 적용)
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();

        invoiceRepository.deleteAll();
        billingHistoryRepository.deleteAll();
        lineRepository.deleteAll();

        savedInvoices = new ArrayList<>();

        line100 = createAndSaveLine(100L, "010-1111-1111", "user1@test.com");
        line200 = createAndSaveLine(200L, "010-2222-2222", "user2@test.com");

        // 유저 A (ID: 100) 데이터 3건
        savedInvoices.add(createInvoice(line100, "2024-01", 15000));
        savedInvoices.add(createInvoice(line100, "2024-02", 14000));
        savedInvoices.add(createInvoice(line100, "2024-03", 16000));

        // 유저 B (ID: 200) 데이터 2건
        savedInvoices.add(createInvoice(line200, "2024-01", 25000));
        savedInvoices.add(createInvoice(line200, "2024-02", 22000));

        savedInvoices = invoiceRepository.saveAll(savedInvoices);
        invoiceRepository.flush();
        this.savedInvoices = invoiceRepository.findAll();
    }

    private Line createAndSaveLine(Long tempId, String phone, String email) {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .email(email)
                .name("테스트 유저")
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "createdAt", now);
        ReflectionTestUtils.setField(user, "updatedAt", now);
        em.persist(user); // DB에 강제 저장


        DueDate dueDate = DueDate.builder()
                .date(15) // 15일 납부
                .build();
        em.persist(dueDate);

        Line line = Line.builder()
                .user(user)       // 저장된 객체 주입
                .dueDate(dueDate) // 저장된 객체 주입
                .phone(phone)
                .planId(1) // 필수값
                .status(LineStatus.ACTIVE)
                .startDate(now)
                .build();

        // Auditing 수동 주입
        ReflectionTestUtils.setField(line, "createdAt", now);
        ReflectionTestUtils.setField(line, "updatedAt", now);

        return lineRepository.save(line);
    }

    private Invoice createInvoice(Line line, String month, int amount) {
        LocalDateTime now = LocalDateTime.now();

        BillingHistory bh = BillingHistory.builder()
                .line(line)
                .billingMonth(month)
                .amount(BigDecimal.valueOf(amount))
                .benefitAmount(BigDecimal.ZERO)
                .planId(1L) // 필수값
                .usage(100)
                .userAt(now) // 필수값
                .build();

//        ReflectionTestUtils.setField(bh, "createdAt", now);
//        ReflectionTestUtils.setField(bh, "updatedAt", now);

        bh = billingHistoryRepository.save(bh);

        Invoice invoice = Invoice.builder()
                .line(line)
                .billingHistory(bh)
                .billingMonth(month)
                .totalAmount(BigDecimal.valueOf(amount))
                .status(InvoiceStatus.CREATED)
                .dueDate(now.plusDays(15))
                .build();

        ReflectionTestUtils.setField(invoice, "createdAt", now);
        ReflectionTestUtils.setField(invoice, "updatedAt", now);

        return invoice;
    }

    // 1. 전체 목록 조회 (페이징)
    @Test
    @DisplayName("청구서 목록 조회 - Page 객체 검증")
    void getInvoicesPagingTest() throws Exception {
        long realLineId = line100.getId();

        mockMvc.perform(get("/billing/bills")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // [수정] Page 객체는 $.content 안에 데이터가 있습니다.
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].lineId", is((int) realLineId)))
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
        HttpStatus expectedStatus = HttpStatus.NOT_FOUND; // 404
        String expectedMessage = "청구서가 존재하지 않습니다. id=2249397";

        mockMvc.perform(get("/billing/bills/{billId}", 2249397L))
                .andExpect(status().is(expectedStatus.value())) // 실제 응답 코드가 404인지 확인
                .andExpect(jsonPath("$.success", is(false))) // success 필드가 false인지 확인
                .andExpect(jsonPath("$.error", is("NOT_FOUND: "+expectedMessage))) // 내가 원하는 메시지인지 확인
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // 4. 유니크 키(회선+월) 조회
    @Test
    @DisplayName("회선번호와 청구월로 조회")
    void getInvoiceByKeyTest() throws Exception {
        String realLineId = String.valueOf(line200.getId());

        mockMvc.perform(get("/billing/bills/byKey")
                        .param("lineId", realLineId)
                        .param("billingMonth", "2024-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineId", is(line200.getId().intValue())))
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