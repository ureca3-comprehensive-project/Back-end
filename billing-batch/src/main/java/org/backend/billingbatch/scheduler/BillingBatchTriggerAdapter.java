package org.backend.billingbatch.scheduler;

import lombok.RequiredArgsConstructor;
import org.backend.port.BatchCommand;
import org.backend.port.BatchTriggerPort;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BillingBatchTriggerAdapter implements BatchTriggerPort {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

    @Override
    public void trigger(BatchCommand command) {
        if(!billingJob.getName().equals(command.jobName())){
            return;
        }

        try{
            JobParameters jobParameters =
                    new JobParametersBuilder()
                            .addString("date", command.date())
                            .toJobParameters();

            jobLauncher.run(billingJob, jobParameters);
        }catch(Exception e){
            throw new RuntimeException("배치 실행 실패",e);
        }

    }
}
