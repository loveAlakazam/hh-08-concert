package io.hhplus.concert.domain.common.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
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

	/**
	 * 페이지네이션 관련 정책
	 */
	public static final int PAGE_SIZE = 10; // 1페이지당 최대 10개의 항목을 갖는다.
	public static final int MINIMUM_PAGE = 1; // 페이지는 1부터 시작한다.

	public final static String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

	/**
	 * 페이지네이션 페이지 유효성 검증<br>
	 *
	 * 페이지의 1페이지가 시작페이지이므로
	 * 페이지값은 1이상의 양수여야한다.
	 *
	 * @param page
	 */
	public static void validatePage(int page) {
		if(page < MINIMUM_PAGE) throw new InvalidValidationException(INVALID_PAGE);
	}


	/**
	 * 공백문자제거 정규표현식
	 */
	public static final String REGEX_REMOVE_WHITESPACE = "\\s+";

	public static String getRegexRemoveWhitespace(String input) {
		return input.replaceAll(REGEX_REMOVE_WHITESPACE, "");
	}

	/**
	 * 엔티티 식별자 유효성 검증<br>
	 *
	 * 엔티티 식별자는 양수여야한다.
	 * @param id
	 */
	public static void validateId(long id) {
		if(id <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);
	}

	/**
	 * 과거날짜 확인 검증 책임 <br><br>
	 * 과거날짜 확인 검증함수<br>
	 *
	 * @param date - 날짜(YYYY-mm-dd)
	 * @return boolean
	 */
	public static boolean isPastDate(LocalDate date) {
		LocalDate now = LocalDate.now();

		// date 가 now 보다 과거일 경우
		return date.isBefore(now);
	}

	/**
	 * 과거일자 확인 검증함수<br>
	 * @param dateTime - 일자(YYYY-mm-ddThh:mm:ss)
	 * @return boolean
	 */
	public static boolean isPastDateTime(LocalDateTime dateTime) {
		LocalDateTime now = LocalDateTime.now();

		// dateTime 이 now 보다 과거일경우
		return dateTime.isBefore(now);
	}
}
