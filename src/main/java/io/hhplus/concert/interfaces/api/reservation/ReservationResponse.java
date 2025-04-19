package io.hhplus.concert.interfaces.api.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.application.usecase.reservation.ReservationResult;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.user.User;

public record ReservationResponse(){
	public record ReserveConcertSeat(
		long reservation_id, // 예약 아이디
		long userId, // 유저 아이디
		ReservationStatus status, // 예약 상태
		LocalDateTime tempReservationExpiredAt, // 임시예약 만료날짜
		long concertId, // 콘서트 아이디
		String name, // 콘서트명
		String artistName, // 아티스트명
		long concertDateId, // 콘서트일정 아이디
		LocalDate progressDate, // 콘서트 날짜
		long concertSeatId, // 좌석 아이디
		int number, // 좌석 번호
		boolean availableSeat// 좌석 예약 가능여부
	) {
		public static ReserveConcertSeat from(ReservationResult.ReserveConcertSeat result) {
			Reservation reservation = result.reservation();
			Concert concert = reservation.getConcert();
			ConcertDate concertDate = reservation.getConcertDate();
			ConcertSeat concertSeat = reservation.getConcertSeat();
			User user = reservation.getUser();
			return new ReserveConcertSeat(
				reservation.getId(), // 예약 아이디
				user.getId(), // 유저 아이디
				reservation.getStatus(), // 예약 상태
				reservation.getTempReservationExpiredAt(), // 임시예약 만료날짜
				concert.getId(), // 콘서트 아이디
				concert.getName(),// 콘서트명
				concert.getArtistName(), // 아티스트명
				concertDate.getId(), // 콘서트 일정 아이디
				concertDate.getProgressDate(), // 콘서트 날짜
				concertSeat.getId(), // 좌석 아이디
				concertSeat.getNumber(), // 좌석번호
				concertSeat.isAvailable() // 좌석 예약 가능여부
			);
		}
	}

}
