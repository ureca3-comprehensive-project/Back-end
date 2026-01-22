package org.backend.billingbatch.job;

import jakarta.persistence.EntityManager;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.microPayment.entity.MicroPayment;
import org.backend.billingbatch.repository.BillingHistoryRepository;
import org.backend.billingbatch.repository.InvoiceRepository;
import org.backend.billingbatch.repository.MicroPaymentRepository;
import org.backend.domain.line.entity.Line;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
public class InvoiceJobTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private MicroPaymentRepository microPaymentRepository;

    @Autowired
    private EntityManager em;

    // 초기화를 위해 Repository 추가 주입
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private BillingHistoryRepository billingHistoryRepository;

    // @BeforeEach를 사용하여 테스트 시작 전에 "무조건" 깨끗하게 만듦 - 초기화가 제대로 안되는 것 같아 추가
    @BeforeEach
    void setUp() {
        // 외래키 제약조건 순서 때문에 자식 -> 부모 순으로 지워야 안전
        microPaymentRepository.deleteAll(); // 소액결제 삭제
        invoiceRepository.deleteAll();      // 결과 테이블(Invoice) 삭제
        billingHistoryRepository.deleteAll(); // 원천 데이터 삭제

        // 테스트 데이터 삽입
        String targetMonth = "2024-01";

        // 통신요금 데이터 생성
        Line line = Line.builder().id(1L).build();
        BillingHistory history = BillingHistory.builder()
                .line(line)
                .amount(BigDecimal.valueOf(12000))
                .billingMonth(targetMonth)
                .benefitAmount(BigDecimal.ZERO)
                .usage(100)
                .userAt(LocalDateTime.now())
                .planId(1L)
                .build();

        billingHistoryRepository.save(history);
    }

    @AfterEach
    @Transactional // 테스트 데이터 초기화를 위해 트랜잭션 사용
    void tearDown() {
        microPaymentRepository.deleteAll();
    }

    @Test
    @DisplayName("청구서 생성 배치가 정상적으로 수행된다")
    void invoiceJobIntegrationTest() throws Exception {
        // given
        String month = "2024-01";

        // Job 실행 시 파라미터
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("billingMonth", month)
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus().toString()).isEqualTo("COMPLETED");

        // EntityManager를 직접 사용하여 검증
        List<Invoice> invoices = em.createQuery("SELECT i FROM Invoice i", Invoice.class)
                .getResultList();

        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(13200));
    }

    @ExtendWith(MockitoExtension.class)
    public static class InvoiceProcessorTest {
        @Mock
        private MicroPaymentRepository microPaymentRepository;

        @InjectMocks
        private InvoiceProcessor invoiceProcessor;

        @Test
        @DisplayName("통신요금과 소액결제를 합산하고 부가세 10%를 적용하여 청구서를 생성한다")
        void processTest() throws Exception {
            // given
            String targetMonth = "2024-01";
            // 통신요금 50,000원
            Line line = Line.builder().id(1L).build();
            BillingHistory history = BillingHistory.builder()
                    .line(line)
                    .amount(BigDecimal.valueOf(50000))
                    .billingMonth(targetMonth)
                    .build();

            // 소액결제 2건 (10,000원 + 5,000원)
            List<MicroPayment> mockPayments = Arrays.asList(
                    MicroPayment.builder()
                            .lineId(1)
                            .payMonth(targetMonth)
                            .payPrice(BigDecimal.valueOf(10000))
                            .build(),
                    MicroPayment.builder()
                            .lineId(1)
                            .payMonth(targetMonth)
                            .payPrice(BigDecimal.valueOf(5000))
                            .build()
            );

            given(microPaymentRepository.findByLineIdAndPayMonth(anyLong(), any()))
                    .willReturn(mockPayments);

            // when
            Invoice result = invoiceProcessor.process(history);

            // then
            // 공급가액 = 50,000 + 15,000 = 65,000
            // 최종금액 = 65,000 * 1.1 = 71,500
            assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(71500));
            assertThat(result.getStatus()).isEqualTo("CREATED");
            assertThat(result.getLine()).isEqualTo(1L);

            System.out.println("테스트 성공! 생성된 청구서 금액: " + result.getTotalAmount());
        }
    }
}
