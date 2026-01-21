package org.backend.domain.template.repository;

import java.util.Optional;

import org.backend.domain.message.type.ChannelType;
import org.backend.domain.template.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Integer> {
	
	// 특정 채널 타입(ChannelType)으로 필터링하며 페이징 처리
    Page<Template> findByType(ChannelType type, Pageable pageable);
    
    // 특정 채널 중 updatedAt이 가장 최근인 데이터 1건 조회
    Optional<Template> findTop1ByTypeOrderByUpdatedAtDesc(ChannelType type);

}
