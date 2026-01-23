package org.backend.billing.invoice.controller;

import lombok.RequiredArgsConstructor;
import org.backend.billing.invoice.dto.InvoiceDetailResponse;
import org.backend.billing.invoice.dto.InvoiceResponse;
import org.backend.domain.invoice.entity.Invoice;
import org.backend.billing.invoice.services.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing/bills")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    // 청구서 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getInvoices(Pageable pageable) {
        Page<InvoiceResponse> responses = invoiceService.findAll(pageable)
                .map(InvoiceResponse::fromEntity);

        return ResponseEntity.ok(responses);
    }

    // 청구서 단건 조회
    @GetMapping("/{billId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long billId) {
        return ResponseEntity.ok(invoiceService.findById(billId));
    }
    // 청구서 단건 조회(유니크 키 기반)
    @GetMapping("/byKey")
    public ResponseEntity<InvoiceResponse> getInvoiceByUniqueKey(
            @RequestParam Long lineId,
            @RequestParam String billingMonth) {
        return ResponseEntity.ok(invoiceService.getInvoiceByUniqueKey(lineId, billingMonth));
    }

    // 청구서 상세 항목 조회
    @GetMapping("/{billId}/items")
    public ResponseEntity<List<InvoiceDetailResponse>> getInvoiceDetails(@PathVariable Long billId) {
        List<InvoiceDetailResponse> details = invoiceService.getInvoiceDetails(billId);
        return ResponseEntity.ok(details);
    }

    // 청구서 일괄 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteInvoicesByMonth(@RequestParam String billingMonth) {
        invoiceService.deleteInvoicesByMonth(billingMonth);
        return ResponseEntity.noContent().build();
    }

    // 청구서 삭제
    @DeleteMapping("/{billId}")
    public ResponseEntity<Void> deleteInvoicesByMonth(@PathVariable Long billId) {
        invoiceService.deleteInvoice(billId);
        return ResponseEntity.noContent().build();
    }

//    // 통계 데이터 - 청구서 수, 지난달 대비 증가율
//    @GetMapping("/statistics")
//    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
//        // 오늘 날짜 기준 생성 건수 집계 로직 호출
//        return ResponseEntity.ok(invoiceService.getTodayStatistics());
//    }
//
//    // 청구서 어떤 형태로 보냈는지 확인
//    @GetMapping("/{billId}/messages")
//    public ResponseEntity<List<Map<String, Object>>> getRelatedMessages(@PathVariable Long billId) {
//        List<Map<String, Object>> history = invoiceService.getRelatedMessageHistory(billId);
//        return ResponseEntity.ok(history);
//    }

}
