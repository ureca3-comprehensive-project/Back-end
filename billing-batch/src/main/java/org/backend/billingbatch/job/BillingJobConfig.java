package org.backend.billingbatch.job;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

import org.backend.billingbatch.dto.BillingResponse;
import org.backend.billingbatch.dto.ContractInfo;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private static final String BILLING_READER_QUERY =
                """
                SELECT l.line_id, l.plan_id, l.user_id, p.base_price, pl_voice.limit_amount AS voice_limit, pl_data.limit_amount AS data_limit, ou_voice.additional_price AS voice_unit_price, ou_data.additional_price AS data_unit_price, dp.rate, dp.discount_limit, l.start_date
                FROM Line l
                JOIN Plan p on l.plan_id = p.plan_id
              
                LEFT JOIN PlanItem pl_voice on l.plan_id = pl_voice.plan_id AND pl_voice.item_type = 'VOICE'
                LEFT JOIN PlanItem pl_data on l.plan_id = pl_data.plan_id AND pl_data.item_type = 'DATA'
                        
                LEFT JOIN OverUsageRule ou_voice on pl_voice.item_id = ou_voice.item_id
                LEFT JOIN OverUsageRule ou_data on pl_data.item_id = ou_data.item_id
                        
                LEFT JOIN LineDiscount ld on l.line_id = ld.line_id
                LEFT JOIN DiscountPolicy dp on ld.policy_id = dp.policy_id
                
                WHERE l.status = 'ACTIVE';
                """;
    private static final String BILLING_WRITER_QUERY =
            """
            INSERT INTO BillingHistory (line_id,plan_id,`usage`,amount,user_at,billing_month,benefit_amount)
            VALUES (:line_id, :plan_id, :usage, :amount, :userAt, :billingMonth, :benefitAmount)
            """;

    @Bean
    public Job billingJob(Step billingStep){
        return new JobBuilder("billingJob", jobRepository)
                .start(billingStep)
                .build();
    }

    @Bean
    public Step billingStep(BillingItemProcessor billingItemProcessor){
        return new StepBuilder("billingStep",jobRepository)
                .<ContractInfo, BillingResponse>chunk(1000).transactionManager(transactionManager)
                .reader(billingReader())
                .processor(billingItemProcessor)
                .writer(billingWriter())
                .build();

    }

    @Bean
    public JdbcCursorItemReader<ContractInfo> billingReader(){
        return new JdbcCursorItemReaderBuilder<ContractInfo>()
                .name("billingReader")
                .dataSource(dataSource)
                .sql(BILLING_READER_QUERY)
                .rowMapper(new BeanPropertyRowMapper<>(ContractInfo.class))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<BillingResponse> billingWriter(){
        return new JdbcBatchItemWriterBuilder<BillingResponse>()
                .dataSource(dataSource)
                .sql(BILLING_WRITER_QUERY)
                .beanMapped()
                .build();
    }

}
