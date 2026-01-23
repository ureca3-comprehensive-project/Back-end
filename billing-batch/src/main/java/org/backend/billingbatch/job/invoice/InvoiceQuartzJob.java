package org.backend.billingbatch.job.invoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceQuartzJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job createInvoiceJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 오늘 날짜 + 3일 계산
        // ex. 오늘이 8일이면 -> 11일이 타겟
        // ex. 오늘이 1월 30일이면 -> 2월 2일이 타겟 (알아서 넘어감)
        LocalDate targetDate = LocalDate.now().plusDays(3);
        int targetDay = targetDate.getDayOfMonth();

        // 청구 월 (보통 청구서는 지난달 사용량을 청구하므로 지난달로 설정함)
        String billingMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        log.info("청구서 생성 배치 시작! 타겟 납부일: {}일 (청구월: {})", targetDay, billingMonth);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("billingMonth", billingMonth)
                    .addLong("targetDay", (long) targetDay) // 3일 뒤 날짜
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(createInvoiceJob, jobParameters);

        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            throw new JobExecutionException(e);
        }
    }
}