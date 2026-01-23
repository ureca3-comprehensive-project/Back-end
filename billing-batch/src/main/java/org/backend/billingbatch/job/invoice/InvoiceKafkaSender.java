package org.backend.billingbatch.job.invoice;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceDto;
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

    @Override
//    public void write(Chunk<? extends Invoice> chunk) {
    public void write(Chunk<? extends InvoiceDto> chunk) { // invoiceDto 버전
        Page<Template> templates = templateRepository.findByType(ChannelType.EMAIL, PageRequest.of(0, 1));
        Template emailTemplate = templates.getContent().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("EMAIL용 청구서 템플릿이 존재하지 않습니다."));

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
        String sql = "INSERT INTO Message (invoice_id, template_id, channel_type, status, dedup_key, correlation_id, retry_count, max_retry, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        jdbcTemplate.batchUpdate(sql, chunk.getItems(), chunk.size(), (ps, item) -> {
            ps.setString(1, item.invoiceId()); // UUID 사용
            ps.setInt(2, emailTemplate.getId());
            ps.setString(3, ChannelType.EMAIL.name());
            ps.setString(4, MessageStatus.PENDING.name());
            ps.setString(5, "INV-" + item.billingMonth() + "-" + item.billingId()); // 중복 키
            ps.setString(6, UUID.randomUUID().toString());
            ps.setInt(7, 0);
            ps.setInt(8, 3);
        });
    }
}
