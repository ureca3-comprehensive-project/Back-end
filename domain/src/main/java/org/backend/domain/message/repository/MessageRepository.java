package org.backend.domain.message.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.type.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	
	@Query("""
			SELECT m FROM Message m
			WHERE m.status in (:statuses)
			  AND m.availableAt <= :now
			  AND m.retryCount < m.maxRetry
			""")
	List<Message> findPendingMessages(@Param("statuses") List<MessageStatus> statuses,
		    						  @Param("now") LocalDateTime now,
		    						   Pageable pageable);

}
