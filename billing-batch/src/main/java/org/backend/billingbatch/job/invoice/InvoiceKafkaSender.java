package org.backend.billingbatch.job.invoice;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceDto;
import org.backend.domain.invoice.entity.IdGenerator;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.message.type.MessageStatus;
import org.backend.domain.template.entity.Template;
import org.backend.domain.template.repository.TemplateRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

// kafka에게 청구서 배치를 메시지로 만들어서 전송
@Component
@RequiredArgsConstructor
//public class InvoiceKafkaSender implements ItemWriter<Invoice> {
public class InvoiceKafkaSender implements ItemWriter<InvoiceDto> { // invoiceDto 버전
    private final MessageRepository messageRepository;
    private final TemplateRepository templateRepository;
    private final JdbcTemplate jdbcTemplate;
    private final IdGenerator idGenerator;
    private Template cachedEmailTemplate; // 캐싱용 변수

    @Override
//    public void write(Chunk<? extends Invoice> chunk) {
    public void write(Chunk<? extends InvoiceDto> chunk) { // invoiceDto 버전
//        Page<Template> templates = templateRepository.findByType(ChannelType.EMAIL, PageRequest.of(0, 1));
//        Template emailTemplate = templates.getContent().stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("EMAIL용 청구서 템플릿이 존재하지 않습니다."));

        if (cachedEmailTemplate == null) {
            cachedEmailTemplate = templateRepository.findByType(ChannelType.EMAIL, PageRequest.of(0, 1))
                    .getContent().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("EMAIL용 청구서 템플릿이 없습니다."));
        }


//        for (Invoice invoice : chunk) {
//            // 메시지 엔티티 생성
//            Message message = Message.builder()
//                    .invoice(invoice)
//                    .template(emailTemplate)
//                    .channelType(ChannelType.EMAIL) // 기본 채널인 이메일로 설정
//                    .status(MessageStatus.PENDING) // 상태는 대기로 설정하여 바로 kafka에서 처리 할 수 있도록 설정
//                    .dedupKey("INV-" + invoice.getBillingMonth() + "-" + invoice.getId()) // 중복 발송 방지 키
//                    .correlationId(UUID.randomUUID().toString())
//                    .retryCount(0)
//                    .maxRetry(3)
//                    .build();
//
//            messageRepository.save(message);
//        }

        // invoiceDto 버전
        String sql = "INSERT INTO message (message_id, invoice_id, template_id, channel_type, status, dedup_key, correlation_id, retry_count, max_retry, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try {
            jdbcTemplate.batchUpdate(sql, chunk.getItems(), chunk.size(), (ps, item) -> {
                ps.setLong(1, idGenerator.nextId()); // PK
                ps.setLong(2, item.invoiceId());    // FK (Invoice)
                ps.setInt(3, cachedEmailTemplate.getId());
                ps.setString(4, ChannelType.EMAIL.name());
                ps.setString(5, MessageStatus.PENDING.name());
                ps.setString(6, "INV-" + item.billingMonth() + "-" + item.billingId() + "-" + UUID.randomUUID().toString().substring(0,5)); // 중복 방지 강화
                ps.setString(7, UUID.randomUUID().toString());
                ps.setInt(8, 0);
                ps.setInt(9, 3);
            });
            System.out.println(">>> [KafkaSender] " + chunk.size() + "건의 메시지 DB 삽입 완료");
        } catch (Exception e) {
            System.err.println(">>> [KafkaSender] 메시지 삽입 중 에러 발생: " + e.getMessage());
            throw e; // 배치 흐름상 예외를 던져야 롤백됨
        }
    }
}
