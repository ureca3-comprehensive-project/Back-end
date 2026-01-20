package org.backend.billingbatch.job;

import org.backend.billingbatch.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepositoryForTest extends JpaRepository<Invoice, Long> {
}
