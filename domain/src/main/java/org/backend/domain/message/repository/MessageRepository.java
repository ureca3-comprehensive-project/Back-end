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

	@Query("SELECT m.channelType, COUNT(m) FROM Message m GROUP BY m.channelType")
	List<Object[]> countByChannelType();

	// 대시보드용: 최근 실패한 메시지 5건 조회
	List<Message> findTop5ByStatusOrderByCreatedAtDesc(MessageStatus status);

	// 대시보드용: 특정 상태 및 생성일 이후 건수 집계
	long countByStatusAndCreatedAtAfter(MessageStatus status, LocalDateTime dateTime);
}
