package io.hhplus.concert.domain.reservation.service;

import static io.hhplus.concert.domain.reservation.entity.Reservation.*;
import static io.hhplus.concert.domain.reservation.entity.ReservationStatus.*;
import static io.hhplus.concert.domain.reservation.exceptions.messages.ReservationExceptionMessage.*;
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

import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.entity.ReservationStatus;
import io.hhplus.concert.domain.reservation.repository.ReservationRepository;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.interfaces.api.reservation.dto.ReservationDetailResponse;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;

	@BeforeEach
	void setUp() {
		reservationService = new ReservationService(reservationRepository);
	}
	@Test
	void 예약정보조회시_예약id에_맞는_응답데이터가_null이면_않으면_NotFoundException_예외를_발생한다() {
		// given
		long id = 1L;
		when(reservationRepository.getReservationDetailInfo(id)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> reservationService.getReservationDetailInfo(id)
		);

		assertEquals( NOT_FOUND_RESERVATION, ex.getMessage()); // 메시지 검증
	}
	@Test
	void 예약정보조회시_예약id에_맞는_응답데이터가_존재하면_예약조회를_성공한다() {
		// given
		long id = 1L;

		LocalDateTime nowDateTime = LocalDateTime.now();
		LocalDate nowDate = LocalDate.now();

		LocalDateTime temporaryExpiredAt = nowDateTime.plusMinutes(TEMPORARY_RESERVATION_DURATION_MINUTE);
		LocalDate concertDate = nowDate.plusWeeks(2);

		// 임시로 예약된 상태
		ReservationDetailResponse expected =  new ReservationDetailResponse(
			1L,
			"예약자명",
			1L,
			"테스트용 콘서트명 입니다",
			"테스트용 콘서트 아티스트 입니다",
			concertDate,
			"콘서트 장소입니다.",
			1,
			15000,
			ReservationStatus.PENDING_PAYMENT,
			null,
			temporaryExpiredAt
		);
		when(reservationRepository.getReservationDetailInfo(id)).thenReturn(expected);

		// when & then
		ReservationDetailResponse result = assertDoesNotThrow(() -> reservationService.getReservationDetailInfo(id));
		assertEquals(expected.reservationId(), result.reservationId()); // 예약 아이디 검증
		assertEquals(expected.userName(), result.userName()); // 예약자 이름 검증
		assertEquals(expected.userId(), result.userId()); // 예약자 아이디 검증
		assertEquals(expected.concertName(), result.concertName()); // 예약 콘서트명 검증
		assertEquals(expected.artistName(), result.artistName()); // 예약 콘서트 아티스트명 검증
		assertEquals(expected.concertDate() , result.concertDate()); // 예약 콘서트 날짜 검증
		assertEquals(expected.concertLocation(), result.concertLocation()); // 예약 콘서트 장소 검증
		assertEquals(expected.status(), result.status()); // 예약 상태 검증
		assertEquals(expected.reservedAt(), result.reservedAt()); // 예약 확정 일자 검증
		assertEquals(expected.tempReservationExpiredAt(), result.tempReservationExpiredAt()); // 임시예약 만료일자 검증
	}

	@Test
	void 임시예약_만료일자가_이미_지나가버린_경우_RequestTimeOutException_예외발생() {
		// given
		long userId = 1L;
		long concertSeatId = 1L;

		User user = new User(userId, "은강");
		ConcertSeat concertSeat = new ConcertSeat(concertSeatId, 5, 15000, true);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime alreadyExpired = now.minusSeconds(1);

		// 이미 임시예약 만료일자가 지난 예약데이터
		Reservation alreadyExpiredTemporaryReservedReservation = new Reservation(
			PENDING_PAYMENT,
			null,
			alreadyExpired
		);
		when(reservationRepository.findByConcertSeatIdAndUserId(userId, concertSeatId))
			.thenReturn(alreadyExpiredTemporaryReservedReservation);

		// when & then
		RequestTimeOutException ex = assertThrows(
			RequestTimeOutException.class,
			() -> reservationService.reserveOrUpdateTemporaryReservedStatus(user, concertSeat)
		);

		assertEquals(TEMPORARY_RESERVATION_ALREADY_EXPIRED, ex.getMessage());

		// 예외발생이후 로직은 수행하지 않는다
		verify(reservationRepository,never()).saveOrUpdate(any());
	}

}
