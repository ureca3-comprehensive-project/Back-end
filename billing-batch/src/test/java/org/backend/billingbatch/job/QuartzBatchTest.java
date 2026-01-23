package org.backend.billingbatch.job;

import org.backend.billingbatch.job.invoice.InvoiceQuartzJob;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
public class QuartzBatchTest {

    @Autowired private Scheduler scheduler;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        // 1. 데이터 초기화
        jdbcTemplate.execute("DELETE FROM `Invoice`");
        jdbcTemplate.execute("DELETE FROM `BillingHistory`");
        jdbcTemplate.execute("DELETE FROM `MicroPayment`");

        jdbcTemplate.execute("DELETE FROM `Line`");
        jdbcTemplate.execute("DELETE FROM `dueDate`");
        jdbcTemplate.execute("DELETE FROM `User`");
        jdbcTemplate.execute("DELETE FROM `Plan`");

        try {
            jdbcTemplate.execute("CREATE INDEX idx_micropayment_line_month ON micro_payment(line_id, pay_month)");
        } catch (Exception e) {}
    }

    @Test
    @DisplayName("Quartz 트리거 -> 배치 실행 -> 납부일 3일 전 고객만 청구서 생성 확인")
    void quartzTriggerTest() throws Exception {
        // 1. 동적 날짜 계산 (Quartz 로직과 동일하게)
        // 오늘이 1월 20일이면 -> 타겟 납부일은 23일
        LocalDate today = LocalDate.now();
        int targetDay = today.plusDays(3).getDayOfMonth();
        String billingMonth = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        System.out.println("📅 테스트 기준일: " + today);
        System.out.println("🎯 타겟 납부일(3일 뒤): " + targetDay + "일");
        System.out.println("💸 청구 대상월: " + billingMonth);


        // 2. 기초 데이터 세팅
        jdbcTemplate.update("INSERT INTO User (user_id, email, status) VALUES (1, 'test@test.com', 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO Plan (plan_id, name, base_price) VALUES (1, 'Basic Plan', 30000)");

        // 2-2. DueDate 생성 (Quartz가 찾을 타겟 날짜로 설정!)
        // 예: targetDay가 23이면, 23일에 납부하는 due_date 데이터를 만듦
        jdbcTemplate.update("INSERT INTO dueDate (due_date_id, date) VALUES (1, ?)", targetDay);

        // 2-3. Line 생성 (해당 납부일과 연결)
        jdbcTemplate.update("INSERT INTO Line (line_id, user_id, plan_id, due_date_id, phone) VALUES (1, 1, 1, 1, '010-1234-5678')");

        // 2-4. BillingHistory 생성 (청구 데이터)
        // 청구월(billingMonth)이 일치해야 배치가 가져감
        jdbcTemplate.update("INSERT INTO BillingHistory (billing_id, line_id, plan_id, amount, billing_month) VALUES (1, 1, 1, 50000, ?)", billingMonth);

        // 2-5. 납부일이 안 맞는 데이터 추가
        // 날짜가 targetDay + 1인 데이터 -> 청구서 생성 x
        jdbcTemplate.update("INSERT INTO dueDate (due_date_id, date) VALUES (99, ?)", targetDay + 1);
        jdbcTemplate.update("INSERT INTO Line (line_id, user_id, plan_id, due_date_id) VALUES (99, 1, 1, 99)");
        jdbcTemplate.update("INSERT INTO BillingHistory (billing_id, line_id, plan_id, amount, billing_month) VALUES (99, 99, 99, 50000, ?)", billingMonth);


        // 3. Quartz Job 강제 트리거
        JobKey jobKey = JobKey.jobKey("invoiceJob", "DEFAULT");
        if (!scheduler.checkExists(jobKey)) {
            System.out.println("📝 JobDetail이 없어서 테스트에서 직접 등록합니다.");
            JobDetail jobDetail = JobBuilder.newJob(InvoiceQuartzJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            scheduler.addJob(jobDetail, true);
        }

        System.out.println("🚀 Quartz 스케줄러 실행 요청...");
        scheduler.triggerJob(jobKey);


        // 4. 비동기 실행 대기 및 검증 (최대 10초 대기)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = invoiceRepository.count();
            System.out.println("👀 DB 확인 중... 생성된 청구서 수: " + count);

            // 검증 1: 청구서가 정확히 1개만 생성되어야 함 (targetDay 일치하는 고객만)
            assertThat(count).isEqualTo(1);
        });

        // 검증 2: 생성된 청구서 내용 확인
        var invoice = invoiceRepository.findAll().get(0);
        assertThat(invoice.getLine().getId()).isEqualTo(1L); // 타겟 고객 (Line ID 1)
        assertThat(invoice.getBillingMonth()).isEqualTo(billingMonth); // 청구월 확인

        System.out.println("✅ 테스트 성공! 납부일이 " + targetDay + "일인 고객의 청구서만 정확히 생성되었습니다.");
    }
}