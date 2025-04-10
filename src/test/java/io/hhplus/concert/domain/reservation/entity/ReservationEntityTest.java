package io.hhplus.concert.domain.reservation.entity;

import static io.hhplus.concert.domain.reservation.entity.ReservationStatus.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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


}
