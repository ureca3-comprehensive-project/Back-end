package org.backend.billingbatch.repository;

import org.backend.domain.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {


    // 해당 월의 청구서 일괄 삭제
    @Modifying
    @Query("DELETE FROM Invoice i WHERE i.billingMonth = :billingMonth")
    void deleteByBillingMonth(@Param("billingMonth") String billingMonth);

    // 회선ID와 청구월로 조회
    Optional<Invoice> findByLineIdAndBillingMonth(Long lineId, String billingMonth);
}
