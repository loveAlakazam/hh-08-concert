package io.hhplus.concert.application.usecase.payment;

import static io.hhplus.concert.domain.reservation.entity.Reservation.*;
import static io.hhplus.concert.domain.reservation.entity.ReservationStatus.*;
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

import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.domain.payment.service.PaymentService;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.domain.reservation.service.ReservationService;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.service.UserService;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import io.hhplus.concert.interfaces.api.user.dto.PointResponse;

@ExtendWith(MockitoExtension.class)
public class ApplyPaymentTest {
	@InjectMocks
	private PaymentUsecase paymentUsecase;
	@Mock
	private UserService userService;
	@Mock
	private ReservationService reservationService;
	@Mock
	private PaymentService paymentService;

	@BeforeEach
	void setUp() {
		paymentUsecase = new PaymentUsecase(userService, reservationService, paymentService);
	}

	@Test
	void 결제요청을_성공한다() {
		// given
		long userId = 1L;
		long concertSeatId = 1L;
		long reservationId = 1L;
		long price = 15000;

		LocalDateTime now = LocalDateTime.now();

		User user = new User(userId, "은강", 20000);
		ConcertSeat concertSeat = new ConcertSeat(concertSeatId, 5, price, false ); //이미예약된 좌석
		Concert concert = new Concert(1L, "신나는 항해 락콘서트입니다", "아티스트");
		ConcertDate concertDate = new ConcertDate(1L, LocalDate.now() ,true,"선릉역 5번출구 앞" );
		concertSeat.setConcert(concert);
		concertSeat.setConcertDate(concertDate);

		LocalDateTime temporaryReservedExpiredAt = now.plusMinutes(TEMPORARY_RESERVATION_DURATION_MINUTE);
		Reservation reservation = new Reservation(
			reservationId,
			PENDING_PAYMENT,
			null,
			temporaryReservedExpiredAt
		);
		reservation.setUser(user);
		reservation.setConcert(concert);
		reservation.setConcertDate(concertDate);
		reservation.setConcertSeat(concertSeat);
		when(reservationService.checkTemporaryReservedStatus(reservationId)).thenReturn(reservation);

		long paymentId = 1L;
		Payment payment = new Payment(paymentId, price);
		payment.setReservation(reservation);
		when(paymentService.confirmedPayment(reservation, price)).thenReturn(payment);

		PaymentResponse expected = new PaymentResponse(
			paymentId,
			reservationId,
			concertSeatId,
			CONFIRMED,
			reservation.getReservedAt(),
			price,
			concert.getName(),
			concert.getArtistName(),
			concertDate.getProgressDate(),
			concertDate.getPlace(),
			concertSeat.getNumber(),
			userId
		);
		when(paymentService.getPaymentDetailInfo(paymentId)).thenReturn(expected);

		// when
		PaymentResponse result = paymentUsecase.applyPayment(userId, reservationId);

		// then
		assertEquals(expected.status(), result.status());
		assertEquals(expected.confirmedAt(), result.confirmedAt());
		assertEquals(expected.reservationId(), result.reservationId());
	}


}
