package org.backend.billingbatch.repository;

import org.backend.domain.billing.entity.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// reader
public interface BillingHistoryRepository extends JpaRepository<BillingHistory, Long> {
}
