package org.backend.domain.invoice.repository;

import org.backend.domain.invoice.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceDetailsRepository extends JpaRepository<InvoiceDetail, Long> {

}
