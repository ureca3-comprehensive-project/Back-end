package org.backend.core.user.repository;

import java.util.Optional;

import org.backend.core.user.entity.BanTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BanTimeRepository extends JpaRepository<BanTime, Integer>{
	
	@Query("SELECT b FROM BanTime b " +
	           "JOIN b.user u " +
	           "JOIN Line l ON l.user.userId = u.userId " +
	           "JOIN Invoice i ON i.line.lineId = l.lineId " +
	           "JOIN Message m ON m.invoice.invoiceId = i.invoiceId " +
	           "WHERE m.messageId = :messageId")
	    Optional<BanTime> findBanTimeByMessageId(@Param("messageId") Long messageId);
	
	

}
