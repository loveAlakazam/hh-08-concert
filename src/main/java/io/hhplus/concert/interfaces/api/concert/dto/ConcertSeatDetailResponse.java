package io.hhplus.concert.interfaces.api.concert.dto;

/**
 *
 * @param id - 좌석 ID
 * @param number - 좌석 번호 (1~50)
 * @param price - 좌석 가격
 * @param isAvailable - 좌석 예약가능여부 (예약가능: true/ 예약불가능: false)
 * @param concertId - 콘서트 ID
 * @param concertDateId - 콘서트날짜 ID
 */
public record ConcertSeatDetailResponse(
	long id,
	int number,
	long price,
	boolean isAvailable,
	long concertId,
	long concertDateId
) { }
