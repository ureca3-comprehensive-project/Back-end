package org.backend.core.message.repository;

import java.util.List;

import org.backend.core.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
	
	List<Message> findPendingMessages();

}
