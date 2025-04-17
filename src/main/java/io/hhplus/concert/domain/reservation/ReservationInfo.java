package io.hhplus.concert.domain.reservation;

public class ReservationInfo {
	public record TemporaryReserve(Reservation reservation) {
		public static TemporaryReserve from(Reservation reservation) {
			return new TemporaryReserve(reservation);
		}
	}
	public record Cancel(Reservation reservation) {
		public static Cancel from(Reservation reservation) {
			return new Cancel(reservation);
		}
	}
	public record Confirm(Reservation reservation) {
		public static Confirm from(Reservation reservation) {
			return new Confirm(reservation);
		}
	}
	public record Get(Reservation reservation) {
		public static Get from(Reservation reservation) {
			return new Get(reservation);
		}
	}
}
