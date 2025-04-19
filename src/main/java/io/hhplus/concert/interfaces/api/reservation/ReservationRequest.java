package io.hhplus.concert.interfaces.api.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReservationRequest () {
	public record ReserveConcertSeat(
		@Schema(description = "유저 ID", example="123")
		long userId,
		@Schema(description = "좌석 ID", example="5001")
		long concertSeatId
	){}
}

