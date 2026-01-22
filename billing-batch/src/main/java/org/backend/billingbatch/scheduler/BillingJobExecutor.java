package org.backend.billingbatch.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingJobExecutor extends QuartzJobBean {

    private final JobOperator jobOperator;
    private final Job billingJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try{
            log.info("BillingJob 실행");


            String billingMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

            JobParameters jobParameters = new JobParametersBuilder().addString("date",billingMonth)
                    .toJobParameters();

            jobOperator.start(billingJob, jobParameters);

        } catch(Exception e){
            log.error("BillingJob 실행 중 오류 발생", e);
            throw new JobExecutionException(e);
        }

    }



}
