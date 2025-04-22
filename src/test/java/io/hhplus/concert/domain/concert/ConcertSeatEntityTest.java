package io.hhplus.concert.domain.concert;



import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class ConcertSeatEntityTest {
	private static final Logger log = LoggerFactory.getLogger(ConcertSeatEntityTest.class);

	@Test
	void 이미_예약된_좌석을_예약했을시_BusinessException_발생() {
		// given
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of(concert, LocalDate.now(), true, "테스트 장소위치");
		// 이미 예약된 좌석
		ConcertSeat alreadyReservedSeat = ConcertSeat.of(concert, concertDate, 15, 15000, false);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			()-> alreadyReservedSeat.reserve()
		);
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 예약이_가능한_좌석을_예약처리하면_예약불가능_상태로_변경이된다() {
		// given
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of(concert, LocalDate.now(), true, "테스트 장소위치");
		// 예약 가능한 좌석
		ConcertSeat seat = ConcertSeat.of(concert, concertDate, 15, 15000, true);

		// when
		assertDoesNotThrow(()-> seat.reserve());

		// then
		assertEquals(false, seat.isAvailable());
	}
	@Test
	void 이미_예약된좌석을_취소처리하면_예약가능_상태로_변경된다() {
		// given
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of(concert, LocalDate.now(), true, "테스트 장소위치");
		// 이미 예약된 좌석
		ConcertSeat seat = ConcertSeat.of(concert, concertDate, 15, 15000, false);

		// when
		assertDoesNotThrow(()-> seat.cancel());

		// then
		assertEquals(true, seat.isAvailable());
	}
	@Test
	void 예약가능한좌석의_좌석의_가격을_변경할수있다() {
		// given
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of(concert, LocalDate.now(), true, "테스트 장소위치");
		// 예약 가능한 좌석
		ConcertSeat seat = ConcertSeat.of(concert, concertDate, 15, 15000, true);

		// when
		assertDoesNotThrow(() -> seat.editPrice(20000)); // 20000원으로 변경

		// then
		assertEquals(20000, seat.getPrice());
	}
	@Test
	void 이미_예약상태인_좌석의_가격을_변경할_수없으며_변경요청시_BusinessException_발생() {
		// given
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of(concert, LocalDate.now(), true, "테스트 장소위치");
		// 이미 예약 상태인 좌석
		ConcertSeat alreadyReservedSeat = ConcertSeat.of(concert, concertDate, 15, 15000, false);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			()->  alreadyReservedSeat.editPrice(20000) // 20000원으로 변경
		);
		assertNotEquals(20000, alreadyReservedSeat.getPrice()); // 변경이 되지 않았음
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getHttpStatus(), ex.getHttpStatus());
	}
}
