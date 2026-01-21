package org.backend.domain.line.repository;

import org.backend.domain.line.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LineRepository extends JpaRepository<Line, Long> {
	
	@Query("""
	        select l.phone
	        from MessageAttempt ma
	        join ma.message m
	        join m.invoice i
	        join i.line l
	        where ma.id = :attemptId
	    """)
	    String findPhoneByAttemptId(@Param("attemptId") Long attemptId);

}
