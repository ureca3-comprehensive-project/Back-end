package org.backend.billing.message.repository;

import org.backend.billing.message.entity.RetryPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryPolicyRepository extends JpaRepository<RetryPolicyEntity, Long> {
}