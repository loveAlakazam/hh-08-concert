package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static io.hhplus.concert.domain.reservation.entity.Reservation.*;
import static io.hhplus.concert.domain.reservation.entity.ReservationStatus.*;
import static io.hhplus.concert.domain.user.exceptions.messages.UserExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.common.exceptions.ConflictException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.concert.repository.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.service.ConcertService;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.repository.ReservationRepository;
import io.hhplus.concert.domain.reservation.service.ReservationService;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.repository.UserRepository;
import io.hhplus.concert.domain.user.service.UserService;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationDetailResponse;

@ExtendWith(MockitoExtension.class)
public class ApplyConcertSeatReservationTest {
	@InjectMocks
	private ResservationUsecase reservationUsecase;
	@Mock
	private UserService userService;
	@Mock
	private ConcertService concertService;
	@Mock
	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		reservationUsecase = new ResservationUsecase(userService, concertService, reservationService);
	}

	@Test
	void 이미_예약된좌석을_예약요청시_ConflictException_예외발생() {
		// given
		long userId = 1L;
		long concertSeatId = 1L;

		User user = new User(userId, "은강");
		when(userService.getUserEntityById(userId)).thenReturn(user);

		ConcertSeat alreadyReservedConcertSeat = new ConcertSeat(concertSeatId, 5, 15000, false);
		when(concertService.getConcertSeatEntityById(concertSeatId)).thenReturn(alreadyReservedConcertSeat);

		// when & then
		ConflictException ex = assertThrows(
			ConflictException.class,
			() -> reservationUsecase.applyConcertSeatReservation(userId, concertSeatId)
		);
		assertEquals(ALREADY_RESERVED_SEAT, ex.getMessage());

		// 예외발생이후 로직은 수행하지 않는다
		verify(reservationService, never()).reserveOrUpdateTemporaryReservedStatus(user, alreadyReservedConcertSeat);
		verify(concertService, never()).saveOrUpdateConcertSeat(alreadyReservedConcertSeat);
		verify(reservationService,never()).getReservationDetailInfo(anyLong());
	}

	@Test
	void 좌석예약요청에_성공한다() {
		// given
		long userId = 1L;
		long concertSeatId = 1L;
		long reservationId = 1L;

		LocalDateTime now = LocalDateTime.now();

		// user stub
		User user = new User(userId, "은강");
		when(userService.getUserEntityById(userId)).thenReturn(user);

		// ConcertSeat stub
		ConcertSeat concertSeat = new ConcertSeat(concertSeatId, 5, 15000, true);
		Concert concert = new Concert(1L, "신나는 항해 락콘서트입니다", "아티스트");
		ConcertDate concertDate = new ConcertDate(1L, LocalDate.now() ,true,"선릉역 5번출구 앞" );
		concertSeat.setConcert(concert);
		concertSeat.setConcertDate(concertDate);
		when(concertService.getConcertSeatEntityById(concertSeatId)).thenReturn(concertSeat);

		// reservation stub
		LocalDateTime temporaryReservedExpiredAt = now.plusMinutes(TEMPORARY_RESERVATION_DURATION_MINUTE);
		Reservation reservation = new Reservation(
			reservationId,
			PENDING_PAYMENT,
			null,
			temporaryReservedExpiredAt
		);
		when(reservationService.reserveOrUpdateTemporaryReservedStatus(user, concertSeat)).thenReturn(reservation);

		// expected
		ReservationDetailResponse expected = new ReservationDetailResponse(
			reservationId,
			user.getName(),
			userId,
			concertSeat.getConcert().getName(),
			concertSeat.getConcert().getArtistName(),
			concertSeat.getConcertDate().getProgressDate(),
			concertSeat.getConcertDate().getPlace(),
			concertSeat.getNumber(),
			concertSeat.getPrice(),
			reservation.getStatus(),
			reservation.getReservedAt(),
			reservation.getTempReservationExpiredAt()
		);
		when(concertService.saveOrUpdateConcertSeat(concertSeat)).thenReturn(concertSeat);
		when(reservationService.getReservationDetailInfo(reservationId)).thenReturn(expected);

		// when
		ReservationDetailResponse result = reservationUsecase.applyConcertSeatReservation(userId, concertSeatId);

		// then
		assertEquals(expected.tempReservationExpiredAt(), result.tempReservationExpiredAt());
		assertEquals(expected.status(), result.status());
		assertEquals(expected.userName(), result.userName());
		assertEquals(expected.userName(), result.userName());

		// 호출확인
		verify(userService, times(1)).getUserEntityById(userId);
		verify(concertService, times(1)).getConcertSeatEntityById(concertSeatId);
		verify(reservationService, times(1)).reserveOrUpdateTemporaryReservedStatus(user, concertSeat);
		verify(concertService, times(1)).saveOrUpdateConcertSeat(concertSeat);
		verify(reservationService, times(1)).getReservationDetailInfo(reservationId);
	}

}
