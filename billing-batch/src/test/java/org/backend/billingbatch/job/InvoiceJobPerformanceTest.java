package org.backend.billingbatch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// 사용자 100만건, 청구 이력 500만건 => billing history 1249397건
// kafka에게 보내는 거 제외(템플릿 양식 없어서)하고 15일로만 설정(1/4 데이터) 966.16초(약 16분) 걸림(초당 처리량 323.47)
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
public class InvoiceJobPerformanceTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("createInvoiceJob")
    private Job createInvoiceJob;

    @BeforeEach
    void clearJobExecutions() {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(createInvoiceJob);
    }

    @Test
    @DisplayName("100만건 대규모 청구 배치 성능 측정")
    void measurePerformance() throws Exception {
        System.out.println("db 인식");

        // 2. 시간 측정 시작
        long startTime = System.currentTimeMillis();

        // 3. Job 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("billingMonth", "2025-12")
                .addLong("targetDay", 15L)
                .addLong("time", System.currentTimeMillis()) // 매번 새로운 실행을 위해
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 4. 시간 측정 종료
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;

        // 5. 결과 출력
        System.out.println("=========================================");
        System.out.println("배치 수행 상태: " + jobExecution.getStatus());
        System.out.println("총 소요 시간: " + duration + " 초");

        long totalReadCount = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadCount).sum();

        if (duration > 0) {
            System.out.println("처리된 레코드 수: " + totalReadCount);
            System.out.println("초당 처리량(TPS): " + (totalReadCount / duration));
        }
        System.out.println("=========================================");

        assertThat(jobExecution.getStatus().toString()).isEqualTo("COMPLETED");

        // 정합성 확인
        System.out.println("결과 정합성 검증 중...");

        // 1. 생성된 Invoice 개수가 정산 대상 개수와 일치하는지 확인
        Integer actualCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM invoice WHERE billing_month = '2025-12'", Integer.class);

        // 2. 납기일이 15일이 아닌 데이터가 섞여있는지 확인 (0이어야 함)
        Integer errorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM invoice i " +
                        "JOIN line l ON i.line_id = l.line_id " +
                        "JOIN due_date d ON l.due_date_id = d.due_date_id " +
                        "WHERE d.date != 15 AND i.billing_month = '2025-12'", Integer.class);

        System.out.println("생성된 청구서 수: " + actualCount);
        assertThat(actualCount).isGreaterThan(0);
        assertThat(errorCount).isEqualTo(0);
    }
}
