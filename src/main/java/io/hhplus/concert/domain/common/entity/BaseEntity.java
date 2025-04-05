package io.hhplus.concert.domain.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
	@Column(name="created_at")
	private LocalDateTime createdAt;

	@Column(name="updated_at")
	private LocalDateTime updatedAt;
}
