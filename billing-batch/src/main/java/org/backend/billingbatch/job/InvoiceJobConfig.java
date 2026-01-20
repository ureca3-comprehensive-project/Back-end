package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.entity.BillingHistory;
import org.backend.billingbatch.entity.Invoice;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.*;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 배치 설정
@Configuration
@RequiredArgsConstructor
public class InvoiceJobConfig {
    private final DataSource dataSource;
    private final InvoiceProcessor invoiceProcessor;
    private final InvoiceKafkaSender invoiceKafkaSender;

    // 속도 테스트로 변경 가능
    private static final int CHUNK_SIZE = 1000;

    // job - 배치 만들기
    @Bean
    public Job createInvoiceJob(JobRepository jobRepository, Step createInvoiceStep) {
        return new JobBuilder("createInvoiceJob", jobRepository)
                .start(createInvoiceStep)
                .build();
    }

    // step - 배치 과정
    @Bean
    @JobScope
    public Step createInvoiceStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("createInvoiceStep", jobRepository)
                .<BillingHistory, Invoice>chunk(CHUNK_SIZE, transactionManager)
//                .reader(jdbcBillingHistoryReader(null))
                .reader(targetDateBillingReader(null, null))
                .processor(invoiceProcessor)
//                .writer(jdbcInvoiceWriter())
                .writer(compositeInvoiceWriter())
                .build();
    }

    // reader: BillingHistory 테이블에서 해당 월 데이터 읽기
    @Bean
    @StepScope
    public JdbcPagingItemReader<BillingHistory> targetDateBillingReader(
            @Value("#{jobParameters['billingMonth']}") String billingMonth,
            @Value("#{jobParameters['targetDay']}") Long targetDay) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("billingMonth", billingMonth);
        params.put("targetDay", targetDay); // 예: 11 (Quartz에서 넘겨준 값)

        return new JdbcPagingItemReaderBuilder<BillingHistory>()
                .name("targetDateBillingReader")
                .dataSource(dataSource)
                .fetchSize(CHUNK_SIZE)
                .rowMapper(new BeanPropertyRowMapper<>(BillingHistory.class))
                .queryProvider(targetDateQueryProvider(dataSource))
                .parameterValues(params)
                .pageSize(CHUNK_SIZE)
                .build();
    }
//    @Bean
//    @StepScope
//    public JdbcPagingItemReader<BillingHistory> jdbcBillingHistoryReader(
//            @Value("#{jobParameters['billingMonth']}") String billingMonth) throws Exception {
//
//        return new JdbcPagingItemReaderBuilder<BillingHistory>()
//                .name("jdbcBillingHistoryReader")
//                .dataSource(dataSource)
//                .fetchSize(CHUNK_SIZE)
//                .rowMapper(new BeanPropertyRowMapper<>(BillingHistory.class)) // DB 컬럼과 엔티티 필드명 매핑
//                .queryProvider(queryProvider(dataSource))
//                .parameterValues(Collections.singletonMap("billingMonth", billingMonth))
//                .pageSize(CHUNK_SIZE)
//                .build();
//    }

    // 쿼리 자동 생성
    @Bean
    public PagingQueryProvider targetDateQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);

        provider.setSelectClause(
                "SELECT b.billing_id, b.line_id, b.amount, b.billing_month, " +
                        "(SELECT COALESCE(SUM(m.pay_price), 0) FROM MicroPayment m WHERE m.line_id = b.line_id AND m.pay_month = b.billing_month) as microPaymentSum "
        );

        provider.setFromClause(
                "FROM BillingHistory b " +
                        "INNER JOIN Line l ON b.line_id = l.line_id " +
                        "INNER JOIN dueDate d ON l.due_date_id = d.due_date_id "
        );

        // 납부일이 일치하고, 청구월이 일치하는 데이터만 추출
        provider.setWhereClause("WHERE d.date = :targetDay AND b.billing_month = :billingMonth");
        provider.setSortKeys(Collections.singletonMap("b.billing_id", Order.ASCENDING));

        // 전부 정산되 가격이 amount로 올 경우
//        provider.setSelectClause("SELECT b.billing_id, b.line_id, b.amount, b.billing_month");
//
//        provider.setFromClause(
//                "FROM BillingHistory b " +
//                        "INNER JOIN Line l ON b.line_id = l.line_id " +
//                        "INNER JOIN dueDate d ON l.due_date_id = d.due_date_id"
//        );
//
//        provider.setWhereClause("WHERE d.date = :targetDay AND b.billing_month = :billingMonth");
//        provider.setSortKeys(Collections.singletonMap("b.billing_id", Order.ASCENDING));

        return provider.getObject();
    }
//    @Bean
//    public PagingQueryProvider queryProvider(DataSource dataSource) throws Exception {
//        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
//        provider.setDataSource(dataSource);
//
//        provider.setSelectClause("select b.billing_id, b.line_id, b.amount, b.billing_month, " +
//                "(SELECT COALESCE(SUM(m.pay_price), 0) " +
//                " FROM micro_payment m " +
//                " WHERE m.line_id = b.line_id AND m.pay_month = b.billing_month) as microPaymentSum");
//
//        provider.setFromClause("from billing_history b");
//        provider.setWhereClause("where b.billing_month = :billingMonth");
//        provider.setSortKeys(Collections.singletonMap("b.billing_id", Order.ASCENDING));
//
//        return provider.getObject();
//    }

    // Writer - db에 삽입
    @Bean
    public JdbcBatchItemWriter<Invoice> jdbcInvoiceWriter() {
        return new JdbcBatchItemWriterBuilder<Invoice>()
                .dataSource(dataSource)
                .sql("INSERT INTO Invoice (line_id, billing_id, billing_month, total_amount, status, due_date, created_at) " +
                        "VALUES (:lineId, :billingId, :billingMonth, :totalAmount, :status, :dueDate, :createdAt)")
                .beanMapped()
                .build();
    }

    // DB 저장 + Kafka 전송을 동시에 수행하는 Writer
    @Bean
    public CompositeItemWriter<Invoice> compositeInvoiceWriter() {
        return new CompositeItemWriterBuilder<Invoice>()
                .delegates(jdbcInvoiceWriter(), invoiceKafkaSender) // 순서대로 실행(db 삽입 후 kafka 전송)
                .build();
    }
}
