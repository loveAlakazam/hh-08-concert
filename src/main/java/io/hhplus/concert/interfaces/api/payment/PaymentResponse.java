package io.hhplus.concert.interfaces.api.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.application.usecase.payment.PaymentResult;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import lombok.Builder;

@Builder
public record PaymentResponse() {
	public record Execute(
		long paymentId, // 결제 아이디
		long reservationId, // 예약 아이디
		ReservationStatus status, // 예약상태
		LocalDateTime reservedAt, // 예약확정 날짜
		long userId, // 유저아이디
		long concertSeatId, // 콘서트 좌석 아이디
		String concertName, // 콘서트명
		String artistName, //  아티스트명
		LocalDate progressDate, // 콘서트 진행날짜
		String place, // 콘서트 장소
		int concertNumber, // 콘서트 좌석번호
		long price // 콘서트 좌석금액(결제금액)
	) {
		public static Execute from(PaymentResult.PayAndConfirm result) {
			Payment payment = result.payment();
			Reservation reservation = payment.getReservation();
			Concert concert = reservation.getConcert();
			ConcertDate concertDate = reservation.getConcertDate();
			ConcertSeat concertSeat = reservation.getConcertSeat();
			User user = reservation.getUser();

			return new Execute(
				payment.getId(), // 결제 아이디
				reservation.getId(), // 예약 아이디
				reservation.getStatus(), // 예약상태
				reservation.getReservedAt(), // 예약확정 날짜
				user.getId(), // 유저아이디
				concertSeat.getId(), // 콘서트 좌석 아이디
				concert.getName(), // 콘서트명
				concert.getArtistName(), //  아티스트명
				concertDate.getProgressDate(), // 콘서트 진행날짜
				concertDate.getPlace(), // 콘서트 장소
				concertSeat.getNumber(), // 콘서트 좌석번호
				concertSeat.getPrice() // 콘서트 좌석금액(결제금액)
			);
		}
	}
}
