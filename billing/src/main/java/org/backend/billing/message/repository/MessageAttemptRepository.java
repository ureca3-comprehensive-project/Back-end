package org.backend.billing.message.repository;

import org.backend.billing.message.entity.MessageAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAttemptRepository extends JpaRepository<MessageAttemptEntity, Long> {

    long countByMessageId(Long messageId);

    List<MessageAttemptEntity> findTop200ByMessageIdOrderByAttemptNoAsc(Long messageId);
}