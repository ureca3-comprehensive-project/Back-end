package org.backend.domain.user.repository;

import org.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@Query("""
	        select u.email
	        from MessageAttempt ma
	        join ma.message m
	        join m.invoice i
	        join i.line l
	        join l.user u
	        where ma.id = :attemptId
	    """)
	    String findEmailByAttemptId(@Param("attemptId") Long attemptId);

}
