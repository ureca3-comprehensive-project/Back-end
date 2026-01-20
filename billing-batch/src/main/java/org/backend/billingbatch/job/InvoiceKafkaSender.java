package org.backend.billingbatch.job;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceEvent;
import org.backend.billingbatch.entity.Invoice;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// kafka에게 청구서 배치 전송
@Component
@RequiredArgsConstructor
public class InvoiceKafkaSender implements ItemWriter<Invoice> {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "invoice-created-topic";

    @Override
    public void write(Chunk<? extends Invoice> chunk) {

        if (kafkaTemplate == null) {
            throw new IllegalStateException("KafkaTemplate이 주입되지 않았습니다.");
        }

        for (Invoice invoice : chunk) {
            // Entity -> Event DTO로 변환
            InvoiceEvent event = InvoiceEvent.builder()
                    .invoiceId(invoice.getInvoiceId())
                    .lineId(invoice.getLineId())
                    .billingMonth(invoice.getBillingMonth())
                    .totalAmount(invoice.getTotalAmount().longValue())
                    .messageType("INVOICE_CREATED")
                    .build();

            // Kafka 전송 (Key: lineId, Value: event) - 실제 받아서 처리하는 방식에 따라 변경 필요
            // lineId를 String으로 변환(충돌 줄이기 위함)해서 Key로 사용 (순서 보장)
            kafkaTemplate.send(TOPIC, String.valueOf(invoice.getLineId()), event);
        }
    }
}
