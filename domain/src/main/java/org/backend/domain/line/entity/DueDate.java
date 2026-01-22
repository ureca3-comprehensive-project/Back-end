package org.backend.domain.line.entity;

import java.time.LocalDateTime;

import org.backend.domain.line.type.LineStatus;
import org.backend.domain.user.entity.User;
import org.hibernate.annotations.DialectOverride.GeneratedColumn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "due_date")
public class DueDate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "due_date_id")
	private int id;
	
	@Column(name = "date", nullable = false)
	private int date;

}
