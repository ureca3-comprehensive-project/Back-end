package org.backend.domain.message.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.type.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT m FROM Message m
			WHERE m.status in (:statuses)
			  AND m.availableAt <= :now
			  AND m.retryCount < m.maxRetry
			""")
	List<Message> findPendingMessages(@Param("statuses") List<MessageStatus> statuses,
		    						  @Param("now") LocalDateTime now,
		    						   Pageable pageable);

//	// 통계본
//	@Query(value = "SELECT m.channel_type as channelType, m.status as status, " +
//			"m.sent_at as sentAt, t.title as title " +
//			"FROM Message m " +
//			"JOIN Template t ON m.template_id = t.template_id " +
//			"WHERE m.invoice_id = :invoiceId", nativeQuery = true)
//	List<Map<String, Object>> findHistoryByInvoiceId(@Param("invoiceId") Long invoiceId);
//
//	@Query("SELECT m.channelType, COUNT(m) FROM Message m GROUP BY m.channelType")
//	List<Object[]> countByChannelType();

}
