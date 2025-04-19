package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class ConcertEntityTest {
	private static final Logger log = LoggerFactory.getLogger(ConcertEntityTest.class);

	@Test
	void 이미지난날짜로_공연일정을_생성할경우_BusinessException_예외발생() {
		// given
		LocalDate invalidDate = LocalDate.of(2024,4,1);

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> Concert.create("TDD 테스트 콘서트",
				"테스트 아티스트",
				invalidDate,
				"테스트 장소",
				2000L
			)
		);
		assertEquals(PAST_DATE_NOT_AVAILABLE.getMessage(), ex.getMessage());
		assertEquals(PAST_DATE_NOT_AVAILABLE.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 공연일정을_생성을_성공한다() {
		// given
		LocalDate progressDate = LocalDate.now().plusWeeks(1);
		long price = 18000L;

		// when & then
		Concert concert = assertDoesNotThrow(
			()-> Concert.create("TDD 테스트 콘서트", "테스트 아티스트", progressDate, "테스트 장소", price)
		);

		// 공연날짜가 있는지 확인
		assertEquals(1, concert.getDates().size());
		// 해당 공연날짜는 progressDate 와 일치한지 확인
		assertEquals(progressDate, concert.getDates().get(0).getProgressDate());
		// 해당 공연날짜의 좌석은 50개로 구성되어있는지 확인
		assertEquals(50, concert.getDates().get(0).getSeats().size());
		// 각 좌석의 가격은 18000원으로 초기화됐는지 확인
		assertEquals(price, concert.getDates().get(0).getSeats().get(0).getPrice());
		// 각 좌석의 상태는 예약가능한 상태로 초기화됐는지 확인
		assertEquals(true, concert.getDates().get(0).getSeats().get(0).isAvailable());
	}
	@Test
	void 공연일정추가에_성공한다() {
		// given
		LocalDate now = LocalDate.now();
		LocalDate progressDate = now.plusWeeks(1);
		long price = 1500L;

		// 기존에 1개 일정 추가
		Concert concert = Concert.create(
			"TDD 테스트 콘서트", "테스트 아티스트", now, "테스트 장소", 5000L
		);

		// when
		concert.addConcertDate(progressDate, "테스트 장소 2", price);
		// then
		// 공연날짜가 추가되었는지 확인
		assertEquals(2, concert.getDates().size());
		// 추가된 공연 일정 날짜 확인
		assertEquals(progressDate, concert.getDates().get(1).getProgressDate());
		// 추가된 공연 일정 장소 확인
		assertEquals("테스트 장소 2", concert.getDates().get(1).getPlace());
		// 추가된 공연좌석이 50개인지 확인
		assertEquals(50, concert.getDates().get(1).concertSeats().size());
		// 추가된 공연좌석 가격 확인
		assertEquals(price, concert.getDates().get(1).getSeats().get(0).getPrice());
	}

}
