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

	public static int PAGE_SIZE = 10; // 1페이지당 최대 10개의 항목을 갖는다.
	public static int MINIMUM_PAGE = 1; // 페이지는 1부터 시작한다.

	public static String REGEX_REMOVE_WHITESPACE = "\\s+";

	public static String getRegexRemoveWhitespace(String input) {
		return input.replaceAll(REGEX_REMOVE_WHITESPACE, "");
	}

	public static void validateId(long id) {
		if(id <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
	}

	public static void validatePage(int page) {
		if(page < MINIMUM_PAGE) throw new InvalidValidationException(INVALID_PAGE);
	}
}
