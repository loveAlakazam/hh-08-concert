package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertEntityTest;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;

public class ReservationEntityTest {
	private static final Logger log = LoggerFactory.getLogger(ReservationEntityTest.class);
	@Test
	void 예약_생성자를_호출하면_성공한다(){
		// given
		// 유저 초기화
		User user = User.of("테스트");
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);

		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석 - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);

		assertEquals(true, concertDate.isAvailable()); // 공연일정 가능
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능

		// when & then
		// 예약 도메인엔티티 생성
		assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));
	}
	@Test
	void 임시예약하게되면_좌석과_예약의_상태정보가_변경된다() {
		// given
		User user = User.of("테스트"); // 유저 초기화
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);
		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석 - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertEquals(true, concertDate.isAvailable()); // 공연 일정 가능 가능
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능
		Reservation reservation = assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));

		// when
		reservation.temporaryReserve(); // 임시예약
		// then
		assertEquals(false, concertSeat.isAvailable()); // 좌석상태는 이미 예약됨
		assertEquals(PENDING_PAYMENT, reservation.getStatus()); // 예약상태는 '임시예약(PENDING_PAYMENT)'
		assertNotNull( reservation.getTempReservationExpiredAt() ); // 임시예약 만료일자가 존재
		assertNull( reservation.getReservedAt() ); // 예약확정일자는 아직 null
		assertEquals(49, concertDate.countAvailableSeats()); // 해당 콘서트일정에서 예약가능한 좌석개수가 49개인지 확인
	}
	@Test
	void 이미_점유된_좌석을_임시예약_요청하면_BusinessException_예외를_발생() {
		// given
		User user = User.of("테스트"); // 유저 초기화
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);
		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석(이미 점유된상태) - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		concertSeat.reserve(); //예약하려는 좌석이 이미 임시예약된 상태로 설정

		assertEquals(true, concertDate.isAvailable()); // 공연 일정 가능 가능
		assertEquals(false, concertSeat.isAvailable()); // 이미 예약된 상태
		Reservation reservation = assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));

		// when & then
		log.info("이미 예약된 좌석을 예약 신청");
		BusinessException ex = assertThrows(BusinessException.class, () -> reservation.temporaryReserve());

		// when & then
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 임시예약상태에서_예약확정으로_변경() {
		// given
		User user = User.of("테스트"); // 유저 초기화
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);
		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석 - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertEquals(true, concertDate.isAvailable()); // 공연 일정 가능 가능
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능
		Reservation reservation = assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));

		// 좌석 임시예약
		log.info("임시예약 상태로 변경");
		assertDoesNotThrow(() -> reservation.temporaryReserve());
		assertEquals(false, concertSeat.isAvailable()); // 좌석 예약 불가능
		assertEquals(PENDING_PAYMENT, reservation.getStatus()); // 임시예약상태 - PENDING_PAYMENT
		assertNotNull(reservation.getTempReservationExpiredAt()); // 임시예약상태 - 만료일자가 있음
		assertNull(reservation.getReservedAt()); // 임시예약상태 - 예약확정일자 없음
		assertEquals(true, reservation.isTemporary()); // 임시예약상태

		// when
		log.info("5분내로 결제완료했다고 가정해서 예약확정으로 변경");
		assertDoesNotThrow(() -> reservation.confirm());

		// then
		assertEquals(false, concertSeat.isAvailable()); // 좌석 예약 불가능
		assertEquals(CONFIRMED, reservation.getStatus()); // 예약확정상태 - CONFIRMED
		assertNull(reservation.getTempReservationExpiredAt()); // 예약확정상태 - 만료일자 없음
		assertNotNull(reservation.getReservedAt());// 예약확정상태 - 확정일자 있음
	}
	@Test
	void 임시예약_만료일자가_이미_지나간경우_임시예약상태에서_취소상태로_변경한다() throws InterruptedException {
		// given
		User user = User.of("테스트"); // 유저 초기화
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);
		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석 - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertEquals(true, concertDate.isAvailable()); // 공연 일정 가능 가능
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능
		Reservation reservation = assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));

		// 좌석 임시예약
		log.info("임시예약 상태로 변경");
		assertDoesNotThrow(() -> reservation.temporaryReserve());
		assertEquals(false, concertSeat.isAvailable()); // 좌석 예약 불가능
		assertEquals(PENDING_PAYMENT, reservation.getStatus()); // 임시예약상태 - PENDING_PAYMENT
		assertNotNull(reservation.getTempReservationExpiredAt()); // 임시예약상태 - 만료일자가 있음
		assertNull(reservation.getReservedAt()); // 임시예약상태 - 예약확정일자 없음
		assertEquals(true, reservation.isTemporary()); // 임시예약상태

		// 임시예약 기간이 만료된다
		log.info("임시예약일자 만료");
		reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1));
		assertTrue( DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt()) );

		// when
		// 예약취소
		log.info("예약 취소 상태로 변경");
		reservation.cancel();
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능
		assertEquals(CANCELED, reservation.getStatus()); // 취소 상태 - CANCELED
		assertTrue(DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt())); // 이미 만료일자가 과거인지 확인
	}
	@Test
	void 임시예약_만료일자가_유효한_임시예약상태에서_취소요청하면_유효기간이_만료되지않으므로_실패한다() throws InterruptedException {
		// given
		User user = User.of("테스트"); // 유저 초기화
		// 콘서트, 콘서트일정, 콘서트 좌석 초기화
		Concert concert = Concert.create(
			"테스트 콘서트",
			"테스트 아티스트",
			LocalDate.now(),
			"테스트 콘서트 장소",
			15000
		);
		// 예약하려는 콘서트 - concert
		// 예약하려는 콘서트일정 - concertDate
		// 예약하려는 콘서트좌석 - concertSeat
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertEquals(true, concertDate.isAvailable()); // 공연 일정 가능 가능
		assertEquals(true, concertSeat.isAvailable()); // 좌석 예약 가능
		Reservation reservation = assertDoesNotThrow(() -> Reservation.of(user, concert, concertDate, concertSeat));

		// 좌석 임시예약
		log.info("임시예약 상태로 변경");
		assertDoesNotThrow(() -> reservation.temporaryReserve());
		assertEquals(false, concertSeat.isAvailable()); // 좌석 예약 불가능
		assertEquals(PENDING_PAYMENT, reservation.getStatus()); // 임시예약상태 - PENDING_PAYMENT
		assertNotNull(reservation.getTempReservationExpiredAt()); // 임시예약상태 - 만료일자가 있음
		assertNull(reservation.getReservedAt()); // 임시예약상태 - 예약확정일자 없음
		assertEquals(true, reservation.isTemporary()); // 임시예약상태

		// when & then
		log.info("임시예약 만료일자가 만료되지않은 상태에서 예약취소 요청");
		assertFalse(DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt()));
		BusinessException ex = assertThrows(BusinessException.class, () -> reservation.cancel());

		assertEquals(INVALID_ACCESS.getMessage(), ex.getMessage());
		assertEquals(INVALID_ACCESS.getHttpStatus(), ex.getHttpStatus());
		assertTrue(reservation.isTemporary()); // 임시예약상태
	}
}
