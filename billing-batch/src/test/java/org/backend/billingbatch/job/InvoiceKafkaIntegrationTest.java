package org.backend.billingbatch.job;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Import(KafkaAutoConfiguration.class)
class InvoiceKafkaIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @BeforeEach
    void setUp() {
        // 초기화
        jdbcTemplate.execute("DELETE FROM `Invoice`");
        jdbcTemplate.execute("DELETE FROM `BillingHistory`");
        jdbcTemplate.execute("DELETE FROM `Line`");
        jdbcTemplate.execute("DELETE FROM `dueDate`");

        // 데이터 삽입
        jdbcTemplate.execute("INSERT INTO `dueDate` (due_date_id, date) VALUES (1, 11)");
        jdbcTemplate.execute("INSERT INTO `Line` (line_id, user_id, plan_id, due_date_id, phone, status) VALUES (100, 1, 1, 1, '010-1234-5678', 'ACTIVE')");
        jdbcTemplate.execute("INSERT INTO `BillingHistory` (billing_id, line_id, plan_id, amount, billing_month, `usage`) VALUES (1, 100, 1, 55000, '2024-01', 100)");

        // 실제로 조인이 되는지 미리 확인
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM `BillingHistory` b " +
                        "INNER JOIN `Line` l ON b.line_id = l.line_id " +
                        "INNER JOIN `dueDate` d ON l.due_date_id = d.due_date_id " +
                        "WHERE d.date = 11 AND b.billing_month = '2024-01'", Integer.class);

        System.out.println("✅ 테스트 준비 완료! 조인된 데이터 건수: " + count);
    }

    @Test
    @DisplayName("배치가 돌면 DB 저장과 동시에 Kafka 메시지가 발행되어야 한다")
    void testKafkaEmission() throws Exception {
        // 1. Consumer 설정
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-integration-group-" + System.currentTimeMillis());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
        consumer.subscribe(Collections.singleton("invoice-created-topic"));

        // 컨슈머 리밸런싱 및 구독 완료를 위해 아주 잠시 대기
        consumer.poll(Duration.ofMillis(500));

        // 2. 배치 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("billingMonth", "2024-01")
                .addLong("targetDay", 11L)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 배치 건수 확인
        long writeCount = jobExecution.getStepExecutions().iterator().next().getWriteCount();
        System.out.println("📊 배치가 처리한 실제 데이터 건수: " + writeCount);

        // 3. 배치 성공 확인
        assertThat(jobExecution.getStatus().toString()).isEqualTo("COMPLETED");
        assertThat(writeCount).as("배치가 데이터를 1건도 처리하지 못했습니다! 쿼리 조건을 확인하세요.").isGreaterThan(0);

        // 4. 메시지 수신 (최대 10초)
        ConsumerRecord<String, String> foundRecord = null;
        for (int i = 0; i < 10; i++) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            if (!records.isEmpty()) {
                foundRecord = records.iterator().next();
                break;
            }
            System.out.println("...메시지 대기 중 (" + i + ")");
        }

        assertThat(foundRecord).isNotNull();
        System.out.println("✅ 수신된 카프카 메시지: " + foundRecord.value());

        consumer.close();
    }
}