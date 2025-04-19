package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
public class ReserveConcertSeatTest {
	@InjectMocks
	private ReservationUsecase reservationUsecase;
	@Mock
	private UserService userService;
	@Mock
	private ConcertService concertService;
	@Mock
	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		reservationUsecase = new ReservationUsecase(userService, concertService, reservationService);
	}

	private static final Logger log = LoggerFactory.getLogger(ReserveConcertSeatTest.class);
	@Test
	void 이미_예약된좌석을_예약요청시_BusinessException_예외발생() {
		// given
		// 유저
		long userId = 1L;
		User user = User.of( "테스트유저");
		when(userService.getUser(UserCommand.Get.of(userId))).thenReturn(user);
		// 콘서트좌석
		long concertSeatId = 1L;
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);

		log.info("좌석이 이미예약되어있음");
		concertSeat.reserve();
		assertFalse(concertSeat.isAvailable());
		when(concertService.getConcertSeat( ConcertCommand.GetConcertSeat.of(concertSeatId)) )
			.thenReturn(ConcertInfo.GetConcertSeat.from(concertSeat));
		// 예약
		log.info("이미 예약된 좌석을 예약하면 BusinessException 발생");
		when(reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(user, concertSeat))
		).thenThrow(new BusinessException(ALREADY_RESERVED_SEAT));

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of( userId, concertSeatId )
			)
		);
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
	}
	@Test
	void 좌석예약요청에_성공한다() {
		// given
		long userId = 1L;
		User user = User.of( "테스트유저");
		when(userService.getUser(UserCommand.Get.of(userId))).thenReturn(user);

		long concertSeatId = 1L;
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);

		log.info("예약 가능한 좌석");
		assertTrue(concertSeat.isAvailable());
		when(concertService.getConcertSeat( ConcertCommand.GetConcertSeat.of(concertSeatId)) )
			.thenReturn(ConcertInfo.GetConcertSeat.from(concertSeat));

		log.info("임시예약 성공");
		Reservation reservation = Reservation.of(user, concert, concertDate, concertSeat);
		reservation.temporaryReserve();
		assertTrue(reservation.isTemporary());
		ReservationInfo.TemporaryReserve info = ReservationInfo.TemporaryReserve.from(reservation);
		when(reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(user, concertSeat))
		).thenReturn(info);

		// when & then
		ReservationResult.ReserveConcertSeat result = assertDoesNotThrow(
			() -> reservationUsecase.reserveConcertSeat(
					ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertTrue(result.reservation().isTemporary());
		assertFalse(result.reservation().getConcertSeat().isAvailable());
		assertEquals(ReservationStatus.PENDING_PAYMENT, result.reservation().getStatus());
	}


}
