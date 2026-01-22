package org.backend.domain.invoice.repository;

import org.backend.domain.invoice.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    List<InvoiceDetail> findByInvoice_Id(Long invoiceId);

    @Modifying
    @Query("DELETE FROM InvoiceDetail id WHERE id.invoice.id IN " +
            "(SELECT i.id FROM Invoice i WHERE i.billingMonth = :billingMonth)")
    void deleteDetailsByMonth(@Param("billingMonth") String billingMonth);
}
