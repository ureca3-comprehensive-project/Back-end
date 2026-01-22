package org.backend.billingbatch.repository;

import org.backend.domain.microPayment.entity.MicroPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 소액 결제 조회
public interface MicroPaymentRepository extends JpaRepository<MicroPayment, Long> {
    // 해당 회선, 해당 월의 소액결제 내역 조회
    List<MicroPayment> findByLineIdAndPayMonth(Long lineId, String payMonth);
}
