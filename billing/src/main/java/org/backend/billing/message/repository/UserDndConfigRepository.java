package org.backend.billing.message.repository;


import org.backend.billing.message.entity.UserDndConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDndConfigRepository extends JpaRepository<UserDndConfigEntity, Long> {
}