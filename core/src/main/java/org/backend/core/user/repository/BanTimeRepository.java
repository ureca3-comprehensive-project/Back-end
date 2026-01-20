package org.backend.core.user.repository;

import java.util.Optional;

import org.backend.core.user.entity.BanTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BanTimeRepository extends JpaRepository<BanTime, Integer>{
	
	@Query("SELECT b FROM BanTime b " +
			"JOIN b.user u " +
			"JOIN Line l ON l.user = u " +         // Line 엔티티의 user 필드와 조인
			"JOIN Invoice i ON i.line = l " +       // Invoice 엔티티의 line 필드와 조인
			"JOIN Message m ON m.invoice = i " +    // Message 엔티티의 invoice 필드와 조인
			"WHERE m.id = :messageId")
	    Optional<BanTime> findBanTimeByMessageId(@Param("messageId") Long messageId);
	
	

}
