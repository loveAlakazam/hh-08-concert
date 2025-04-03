package io.hhplus.concert.domain.common.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
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

	public static void validateId(long id) {
		if(id <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
	}
}
