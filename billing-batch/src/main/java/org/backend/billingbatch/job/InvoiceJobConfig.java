package org.backend.billingbatch.job;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceDto;
import org.backend.domain.billing.entity.BillingHistory;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.line.entity.Line;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.database.*;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private final EntityManager entityManager;
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
//                .<BillingHistory, Invoice>chunk(CHUNK_SIZE, transactionManager)
                .<BillingHistory, InvoiceDto>chunk(CHUNK_SIZE, transactionManager) // invoiceDto 사용 버전
//                .reader(jdbcBillingHistoryReader(null))
                .reader(targetDateBillingReader(null, null))
                .processor(invoiceProcessor)
//                .writer(jdbcInvoiceWriter())
                .writer(compositeJdbcWriter()) // invoiceDto 사용 버전
//                .writer(compositeInvoiceWriter())
                .taskExecutor(taskExecutor())
                .listener(chunkListener())
                .listener(writeListener())
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
                .saveState(false) // 멀티 쓰레드 정합성 오류 안 생기도록
                .fetchSize(CHUNK_SIZE)
                .rowMapper((rs, rowNum) -> {
                    Line line = Line.builder()
                            .id(rs.getLong("line_id"))
                            .build();

                    return BillingHistory.builder()
                            .id(rs.getLong("billing_id"))
                            .amount(rs.getBigDecimal("amount"))
                            .billingMonth(rs.getString("billing_month"))
                            .line(line)
                            // 임의값 0으로 세팅
                            .planId(0L)
                            .usage(0)
                            .benefitAmount(BigDecimal.ZERO)
                            .userAt(LocalDateTime.now())
                            .build();
                })
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

        // 전부 정산되 가격이 amount로 올 경우
        provider.setSelectClause("SELECT b.billing_id, b.line_id, b.amount, b.billing_month");

//        provider.setFromClause("FROM BillingHistory b " +
//                "INNER JOIN Line l ON b.line_id = l.line_id " +
//                "INNER JOIN dueDate d ON l.due_date_id = d.due_date_id");
//
//        // 납부일이 일치하고, 청구월이 일치하는 데이터만 추출
//        provider.setWhereClause("WHERE b.billing_month = :billingMonth AND d.date = :targetDay");

        // 조인 연산에서 시간을 잡아먹는지, DB 서버 설정이나 I/O 병목현상인지 확인을 위해 작성 => 1397.591 초(23분), 893.96 초당 처리량
        provider.setFromClause("FROM BillingHistory b");
        provider.setWhereClause("WHERE b.billing_month = :billingMonth");

        provider.setSortKeys(Collections.singletonMap("b.billing_id", Order.ASCENDING));

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
                // status enum이어서 .beanMapped()에서 변경
                .itemSqlParameterSourceProvider(item -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    params.addValue("lineId", item.getLine().getId());
                    params.addValue("billingId", item.getBillingHistory().getId());
                    params.addValue("billingMonth", item.getBillingMonth());
                    params.addValue("totalAmount", item.getTotalAmount());
                    params.addValue("status", item.getStatus().name());
                    params.addValue("dueDate", item.getDueDate());
                    params.addValue("createdAt", LocalDateTime.now());
                    return params;
                })
                .build();
    }

    // invoiceDto 사용 버전 =====
    @Bean
    public CompositeItemWriter<InvoiceDto> compositeJdbcWriter() {
        return new CompositeItemWriterBuilder<InvoiceDto>()
                .delegates(invoiceInsertWriter(), invoiceDetailInsertWriter())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<InvoiceDto> invoiceInsertWriter() {
        return new JdbcBatchItemWriterBuilder<InvoiceDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO Invoice (invoice_id, line_id, billing_id, billing_month, total_amount, status, due_date, created_at) " +
                        "VALUES (:invoiceId, :lineId, :billingId, :billingMonth, :totalAmount, :status, :dueDate, NOW())")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<InvoiceDto> invoiceDetailInsertWriter() {
        return new JdbcBatchItemWriterBuilder<InvoiceDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO InvoiceDetail (invoice_detail_id, invoice_id, billing_type, amount, status) " +
                        "VALUES (:detailId, :invoiceId, :billingType, :detailAmount, :detailStatus)")
                .beanMapped()
                .build();
    }
    // ==========

    // DB 저장 + Kafka 전송을 동시에 수행하는 Writer
    @Bean
//    public CompositeItemWriter<Invoice> compositeInvoiceWriter() {
//        return new CompositeItemWriterBuilder<Invoice>()
    public CompositeItemWriter<InvoiceDto> compositeInvoiceWriter() {
        return new CompositeItemWriterBuilder<InvoiceDto>()
//                .delegates(jdbcInvoiceWriter(), invoiceKafkaSender) // 순서대로 실행(db 삽입 후 kafka 전송)
                .delegates(compositeJdbcWriter(), invoiceKafkaSender) // invoiceDto 사용 버전
                .build();
    }

    // 쓰레드 작업
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // 본인 PC CPU 코어 수에 맞게 설정, yml도 수정 필요
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500); // 대기 큐
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 큐가 꽉 차면 호출한 스레드가 직접 처리하게 하여 데이터 유실 방지
        executor.initialize();
        return executor;
    }

    // 로그
    @Bean
    public ChunkListener chunkListener() {
        return new ChunkListener() {
            private long lastTime = System.currentTimeMillis();

            @Override
            public void afterChunk(ChunkContext context) {
                long currentTime = System.currentTimeMillis();
                long diffTime = currentTime - lastTime;
                long count = context.getStepContext().getStepExecution().getReadCount();

                System.out.printf("[%s] [%s]>>> 처리된 레코드: %d건 | 이번 1,000건 소요 시간: %.2f초\n",
                        java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                        Thread.currentThread().getName(),
                        count,
                        diffTime / 1000.0
                );

                lastTime = currentTime;
            }
        };
    }

    // 한 청크가 DB에 써진 후 영속성 컨텍스트를 완전히 비워 메모리 병목을 방지
//    @Bean
//    public ItemWriteListener<Invoice> writeListener() {
//        return new ItemWriteListener<Invoice>() {
//            @Override
//            public void afterWrite(Chunk<? extends Invoice> items) {
//                entityManager.clear();
//            }
//        };
//    }
    @Bean
    public ItemWriteListener<InvoiceDto> writeListener() { // 👈 Invoice -> InvoiceDto
        return new ItemWriteListener<InvoiceDto>() {
            @Override
            public void afterWrite(Chunk<? extends InvoiceDto> items) {
                entityManager.clear();
            }
        };
    }
}
