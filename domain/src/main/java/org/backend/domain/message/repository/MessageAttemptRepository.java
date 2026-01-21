package org.backend.domain.message.repository;

import org.backend.domain.message.entity.MessageAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttemptRepository extends JpaRepository<MessageAttempt, Long> {
	
	long countByMessage_CorrelationId(String correlationId);

}
