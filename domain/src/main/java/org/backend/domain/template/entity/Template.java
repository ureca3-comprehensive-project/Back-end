package org.backend.domain.template.entity;

import org.backend.domain.common.entity.BaseEntity;
import org.backend.domain.message.type.ChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
@Table(name = "template")
public class Template extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ChannelType type;
	
	@Lob
	@Column(name = "title", nullable = false)
	private String title;

	@Lob
	@Column(name = "body", nullable = false)
	private String body;
	
	
	public void update(ChannelType type, String title, String body) {
		this.type = type;
		this.title = title;
		this.body = body;
	}
	

}
