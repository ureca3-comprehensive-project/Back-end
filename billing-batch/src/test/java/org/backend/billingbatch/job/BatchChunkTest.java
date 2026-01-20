package org.backend.billingbatch.job;

import org.backend.billingbatch.dto.BatchRunRequest;
import org.backend.billingbatch.entity.BillingHistory;
import org.backend.billingbatch.repository.BillingHistoryRepository;
import org.backend.billingbatch.repository.InvoiceRepository;
import org.backend.billingbatch.repository.MicroPaymentRepository;
import org.backend.billingbatch.services.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // H2 DB 사용
public class BatchChunkTest {

    @Autowired
    private BatchService batchService; // 가짜(Mock)가 아닌 진짜 서비스 주입

    @Autowired
    private BillingHistoryRepository billingHistoryRepository;

    @Autowired
    private MicroPaymentRepository microPaymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 깨끗한 상태에서 시작
        invoiceRepository.deleteAllInBatch();
        billingHistoryRepository.deleteAllInBatch();
        microPaymentRepository.deleteAllInBatch();

        try {
            jdbcTemplate.execute("CREATE INDEX idx_micropayment_line_month ON micro_payment(line_id, pay_month)");
        } catch (Exception e) {
            // 이미 존재하면 패스 (테스트 반복 실행 시 에러 방지)
        }
    }

    @Test
    @DisplayName("데이터 2500개를 넣으면 청크(1000) 단위로 처리되어 청구서 2500개가 생성된다")
    void chunkProcessingTest() {
        // 1. 대용량 데이터 생성 (2,500건)
        // 청크 사이즈가 1000이므로, 1000 -> 1000 -> 500 이렇게 3번 배치 작동
        int totalCount = 2500;
        String targetMonth = "2024-01";
        List<BillingHistory> dummyData = new ArrayList<>();

        System.out.println("🚀 데이터 " + totalCount + "개 생성 시작...");
        for (long i = 1; i <= totalCount; i++) {
            // lineId는 1부터 2500까지
            dummyData.add(new BillingHistory(i, BigDecimal.valueOf(10000), targetMonth));
        }
        billingHistoryRepository.saveAll(dummyData);
        System.out.println("✅ DB Insert 완료 (BillingHistory: " + billingHistoryRepository.count() + "건)");


        // 2. 배치 실행
        BatchRunRequest request = new BatchRunRequest();

        org.springframework.test.util.ReflectionTestUtils.setField(request, "jobName", "createInvoiceJob");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "billingMonth", targetMonth);

        long executionId = batchService.runJob(request);


        // 3. 결과 검증
        long invoiceCount = invoiceRepository.count();

        System.out.println("🏁 배치 실행 시작!");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        batchService.runJob(request);

        stopWatch.stop();
        System.out.println("=========================================");
        System.out.println("⏱️ 총 수행 시간: " + stopWatch.getTotalTimeSeconds() + "초");
        System.out.println("📊 생성된 청구서 수: " + invoiceCount);
        System.out.println("=========================================");

        assertThat(invoiceCount).isEqualTo(totalCount); // 2500개 확인

        // 첫 번째와 마지막 청구서 확인
        assertThat(invoiceRepository.findAll().get(0).getBillingMonth()).isEqualTo(targetMonth);
    }
}