package org.backend.billing.message.repository;

import org.backend.billing.message.entity.MessageEntity;
import org.backend.billing.message.type.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Optional<MessageEntity> findByDedupKey(String dedupKey);

    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, MessageStatus status);

    List<MessageEntity> findAllByOrderByCreatedAtAsc();

    List<MessageEntity> findByStatusOrderByCreatedAtAsc(MessageStatus status);

    List<MessageEntity> findByScheduledAtIsNotNullOrderByScheduledAtAsc();
    
    List<MessageEntity> findTop200ByUserIdAndStatusOrderByCreatedAtAsc(Long userId, MessageStatus status);
}