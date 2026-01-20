package org.backend.core.message.repository;

import org.backend.core.message.entity.MessageAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttemptRepository extends JpaRepository<MessageAttempt, Long> {

}
