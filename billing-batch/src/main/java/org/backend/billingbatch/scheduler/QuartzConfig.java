package org.backend.billingbatch.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {


    @Bean
    public JobDetail billingJobDetail(){
        return JobBuilder.newJob(BillingJobExecutor.class)
                .withIdentity("billingJob", "batchGroup")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger billingJobTrigger(JobDetail billingJobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(billingJobDetail)
                .withIdentity("billingTrigger","batchGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 30 0 1 * ?"))
//                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build();
    }




}
