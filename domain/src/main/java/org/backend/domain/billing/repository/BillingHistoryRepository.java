package org.backend.domain.billing.repository;

import org.backend.domain.billing.entity.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// reader
public interface BillingHistoryRepository extends JpaRepository<BillingHistory, Long> {
}
