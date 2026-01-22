package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.repository.MessageRepository;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.message.type.MessageStatus;
import org.backend.domain.template.entity.Template;
import org.backend.domain.template.repository.TemplateRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

// kafka에게 청구서 배치를 메시지로 만들어서 전송
@Component
@RequiredArgsConstructor
public class InvoiceKafkaSender implements ItemWriter<Invoice> {

    private final MessageRepository messageRepository;
    private final TemplateRepository templateRepository;

    @Override
    public void write(Chunk<? extends Invoice> chunk) {

        Page<Template> templates = templateRepository.findByType(ChannelType.EMAIL, PageRequest.of(0, 1));
        Template emailTemplate = templates.getContent().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("EMAIL용 청구서 템플릿이 존재하지 않습니다."));


        for (Invoice invoice : chunk) {
            // 메시지 엔티티 생성
            Message message = Message.builder()
                    .invoice(invoice)
                    .template(emailTemplate)
                    .channelType(ChannelType.EMAIL) // 기본 채널인 이메일로 설정
                    .status(MessageStatus.PENDING) // 상태는 대기로 설정하여 바로 kafka에서 처리 할 수 있도록 설정
                    .dedupKey("INV-" + invoice.getBillingMonth() + "-" + invoice.getId()) // 중복 발송 방지 키
                    .correlationId(UUID.randomUUID().toString())
                    .retryCount(0)
                    .maxRetry(3)
                    .build();

            messageRepository.save(message);
        }
    }
}
