package io.hhplus.concert.interfaces.api.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.hhplus.concert.domain.reservation.ReservationStatus;
import lombok.Builder;


/**
 * 예약 상세정보 응답 데이터
 *
 * @param reservationId - 예약 PK (예약번호)
 * @param userName - 예약자명
 * @param userId - 유저 PK
 * @param concertName - 콘서트명
 * @param artistName - 아티스트명
 * @param concertDate - 콘서트 진행 날짜
 * @param concertLocation - 콘서트 장소
 * @param concertSeatNumber - 콘서트 좌석번호(1~50)
 * @param price - 콘서트 좌석 가격
 * @param status - 예약 상태
 * @param reservedAt - 예약확정 일자
 * @param tempReservationExpiredAt - 임시예약 만료일자
 */
@Builder
public record ReservationResponse(
	long reservationId,
	String userName,
	long userId,
	String concertName,
	String artistName,
	LocalDate concertDate,
	String concertLocation,
	int concertSeatNumber,
	long price,
	ReservationStatus status,
	LocalDateTime reservedAt,
	LocalDateTime tempReservationExpiredAt
){ }
