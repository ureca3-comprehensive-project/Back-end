package org.backend.billing.invoice.services;

import lombok.RequiredArgsConstructor;
import org.backend.billing.invoice.dto.InvoiceDetailResponse;
import org.backend.billing.invoice.dto.InvoiceResponse;

import org.backend.billing.invoice.exception.InvoiceNotFoundException;
import org.backend.domain.invoice.repository.InvoiceDetailRepository;
import org.backend.domain.invoice.repository.InvoiceRepository;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.domain.message.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

//    // 추가 기능
//    public Map<String, Object> getTodayStatistics() {
//        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
//        long todayCount = invoiceRepository.countByCreatedAtAfter(todayStart);
//
//        // 지난달 동일 시간대 비교 (예: 1월 23일 vs 12월 23일)
//        LocalDateTime lastMonthStart = todayStart.minusMonths(1);
//        LocalDateTime lastMonthEnd = LocalDateTime.now().minusMonths(1);
//        long lastMonthCount = invoiceRepository.countByCreatedAtBetween(lastMonthStart, lastMonthEnd);
//
//        // 증가율 계산 (분모가 0인 경우 예외처리 포함)
//        double increaseRate = (lastMonthCount == 0) ? 0 :
//                ((double)(todayCount - lastMonthCount) / lastMonthCount) * 100;
//
//        return Map.of(
//                "todayCount", todayCount,
//                "increaseRate", Math.round(increaseRate * 100) / 100.0 // 소수점 둘째자리 반올림
//        );
//    }
//
//    public List<Map<String, Object>> getRelatedMessageHistory(Long billId) {
//        // Invoice ID를 외래키로 가진 Message 테이블 조회
//        // QueryDSL이나 Native SQL을 사용하여 상세 정보(채널명, 발송시간, 성공여부) 반환
//        return messageRepository.findHistoryByInvoiceId(billId);
//    }
//
//    public Map<String, Long> getMessageChannelStatistics() {
//        return messageRepository.countByChannelType().stream()
//                .collect(Collectors.toMap(row -> row[0].toString(), row -> (Long) row[1]));
//    }
}
