package io.hhplus.concert.domain.payment;

import io.hhplus.concert.domain.reservation.Reservation;

public class PaymentCommand {
	public record CreatePayment(Reservation reservation) {
		public static CreatePayment of(Reservation reservation) {
			return new CreatePayment(reservation);
		}
	}
}
