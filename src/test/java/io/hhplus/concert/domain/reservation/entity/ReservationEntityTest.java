package io.hhplus.concert.domain.reservation.entity;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.domain.reservation.ReservationExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.reservation.Reservation;

public class ReservationEntityTest {
	@Test
	void 임시예약상태로_변경후_상태검증에_통과되면_예약객체의_상태는_임시예약상태이다() {
		// given
		Reservation reservation = new Reservation();
		reservation.updateTemporaryReservedStatus();

		// when & then
		assertDoesNotThrow(() -> Reservation.validateTemporaryReservedStatus(reservation));
		assertEquals(PENDING_PAYMENT, reservation.getStatus());
		assertEquals(null, reservation.getReservedAt());
		assertNotNull(reservation.getTempReservationExpiredAt());
	}
	@Test
	void 예약확정으로_변경후_상태검증에_통과되면_예약객체의_상태는_예약확정이다() {
		// given
		Reservation reservation = new Reservation();
		reservation.updateConfirmedStatus();

		// when & then
		assertDoesNotThrow(() -> Reservation.validateConfirmedStatus(reservation));
		assertEquals(CONFIRMED, reservation.getStatus());
		assertNotNull(reservation.getReservedAt());
		assertNull(reservation.getTempReservationExpiredAt());
	}
	@Test
	void 예약상태를_예약확정이_아닌_다른상태값이면_유효성검증실패로_InvalidValidationException_예외발생() {
		// given
		Reservation reservation = new Reservation(PENDING_PAYMENT, null, null);

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> Reservation.validateConfirmedStatus(reservation)
		);
		assertEquals(INVALID_RESERVATION_STATUS, ex.getMessage());
	}

}
