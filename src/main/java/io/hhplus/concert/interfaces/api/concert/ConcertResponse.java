package io.hhplus.concert.interfaces.api.concert;

import java.util.List;

import org.springframework.data.domain.Page;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import lombok.Builder;

@Builder
public record ConcertResponse(long id, String name, String artistName) {
	public static ConcertResponse of(long id, String name, String artistName) {
		return ConcertResponse.builder()
			.id(id)
			.name(name)
			.artistName(artistName)
			.build();
	}
	/**
	 *
	 * @param concertSeatList - 콘서트 좌석리스트
	 */
	public record GetAvailableSeats (
		List<ConcertSeat> concertSeatList
	) {
		public static GetAvailableSeats from(ConcertInfo.GetConcertSeatList info) {
			return new GetAvailableSeats(info.concertSeatList());
		}
	}
	/**
	 *
	 * @param page - 페이지수
	 * @param elements - 현재페이지의 공연개수
	 * @param totalPages - 전체 페이지수
	 * @param totalElements - 전체 공연 개수
	 * @param list - 공연 리스트
	 */
	public record GetConcerts(
		int page,
		int elements,
		int totalPages,
		long totalElements,
		List<Concert> list
	) {
		public static GetConcerts from(ConcertInfo.GetConcertList info) {
			Page<Concert> result = info.concertPage();
			return new GetConcerts(
				result.getNumber() + 1,
				result.getNumberOfElements(),
				result.getTotalPages(),
				result.getTotalElements(),
				result.getContent()
			);
		}
	}

	/**
	 *
	 * @param page - 현재페이지수
	 * @param elements - 현재페이지의 공연일정 개수
	 * @param totalPages - 전체 페이지수
	 * @param totalElements - 전체 공연일정 개수
	 * @param list - 공연일정 리스트
	 */
	public record GetAvailableConcertDates(
		int page,
		int elements,
		int totalPages,
		long totalElements,
		List<ConcertDate> list
	) {
		public static GetAvailableConcertDates from(ConcertInfo.GetConcertDateList info) {
			Page<ConcertDate> result = info.concertDatePage();
			return new GetAvailableConcertDates(
				result.getNumber() + 1,
				result.getNumberOfElements(),
				result.getTotalPages(),
				result.getTotalElements(),
				result.getContent()
			);
		}
	}
}
