package org.backend.billingbatch.repository;

import org.backend.billingbatch.entity.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// reader
public interface BillingHistoryRepository extends JpaRepository<BillingHistory, Long> {
}
