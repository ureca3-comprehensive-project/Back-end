package org.backend.billing.invoice.services;

import java.util.List;
import java.util.stream.Collectors;

import org.backend.billing.invoice.dto.InvoiceDetailResponse;
import org.backend.billing.invoice.dto.InvoiceResponse;
import org.backend.billing.invoice.exception.InvoiceNotFoundException;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.invoice.repository.InvoiceDetailRepository;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.message.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository detailRepository;
    private final MessageRepository messageRepository;

    // 목록 조회 (페이징)
    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    // 단건 조회
    public InvoiceResponse findById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(InvoiceResponse::fromEntity)
                .orElseThrow(() -> new InvoiceNotFoundException("청구서가 존재하지 않습니다. id=" + invoiceId));
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
                .orElseThrow(() -> new InvoiceNotFoundException("존재하지 않는 청구서입니다."));
        invoiceRepository.deleteById(invoiceId);
    }

    public InvoiceResponse getInvoiceByUniqueKey(Long lineId, String billingMonth) {
        return invoiceRepository.findByLineIdAndBillingMonth(lineId, billingMonth)
                .map(InvoiceResponse::fromEntity)
                .orElseThrow(() -> new InvoiceNotFoundException("청구서를 찾을 수 없습니다."));
    }

    public List<InvoiceDetailResponse> getInvoiceDetails(Long invoiceId) {
        return detailRepository.findByInvoice_Id(invoiceId).stream()
                .map(InvoiceDetailResponse::from)
                .collect(Collectors.toList());
    }

}
