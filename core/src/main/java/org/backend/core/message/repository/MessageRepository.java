package org.backend.core.message.repository;

import java.util.List;

import org.backend.core.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	
	@Query("""
			SELECT m FROM Message m
			WHERE m.status = 'PENDING'
			  AND m.availableAt <= :now
			  AND m.retryCount < m.maxRetry
			""")
	List<Message> findPendingMessages();

}
