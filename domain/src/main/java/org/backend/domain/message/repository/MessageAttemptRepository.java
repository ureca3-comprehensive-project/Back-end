package org.backend.domain.message.repository;

import java.util.List;

import org.backend.domain.message.entity.MessageAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttemptRepository extends JpaRepository<MessageAttempt, Long> {
	
	long countByMessage_CorrelationId(String correlationId);
	
	// ✅ message.id로 카운트하려면 이렇게
    long countByMessage_Id(Long messageId);

    // ✅ attempts 조회(상세 화면)
    List<MessageAttempt> findTop200ByMessage_IdOrderByAttemptNoAsc(Long messageId);


}
