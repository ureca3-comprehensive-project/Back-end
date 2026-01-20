package org.backend.billingbatch.services;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.InvoiceDetailResponse;
import org.backend.billingbatch.dto.InvoiceResponse;
import org.backend.billingbatch.entity.Invoice;
import org.backend.billingbatch.repository.InvoiceDetailRepository;
import org.backend.billingbatch.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository detailRepository;

    // 목록 조회 (페이징)
    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    // 단건 조회
    public Invoice findById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("청구서가 존재하지 않습니다. id=" + invoiceId));
    }

    // 월별 일괄 삭제
    @Transactional
    public void deleteInvoicesByMonth(String billingMonth) {
        detailRepository.deleteDetailsByMonth(billingMonth);
        invoiceRepository.deleteByBillingMonth(billingMonth);
    }

    // 단건 삭제
    @Transactional
    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 청구서입니다."));
        invoiceRepository.deleteById(invoiceId);
    }

    public InvoiceResponse getInvoiceByUniqueKey(Long lineId, String billingMonth) {
        Invoice invoice = invoiceRepository.findByLineIdAndBillingMonth(lineId, billingMonth)
                .orElseThrow(() -> new IllegalArgumentException("청구서를 찾을 수 없습니다."));

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .lineId(invoice.getLineId())
                .billingMonth(invoice.getBillingMonth())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .build();
    }

    public List<InvoiceDetailResponse> getInvoiceDetails(Long invoiceId) {
        return detailRepository.findByInvoice_InvoiceId(invoiceId).stream()
                .map(InvoiceDetailResponse::from)
                .collect(Collectors.toList());
    }
}
