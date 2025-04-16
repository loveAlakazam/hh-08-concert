package io.hhplus.concert.domain.common;


import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
	@Column(name="created_at")
	protected LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name="updated_at")
	protected LocalDateTime updatedAt;

	@Column(nullable = false)
	protected boolean deleted = false;

}
