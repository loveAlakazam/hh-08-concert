package io.hhplus.concert.interfaces.api.concert;

import java.util.List;

import org.springframework.data.domain.Page;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import lombok.Builder;

@Builder
public record ConcertResponse(long id, String name, String artistName) { // Page<ConcertDate> concertDatePage
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
		List<ConcertInfo.ConcertSeatListDto> concertSeatList
	) {
		public static GetAvailableSeats from(ConcertInfo.GetConcertSeatList info) {
			return new GetAvailableSeats(info.concertSeatList());
		}
	}
	/**
	 *
	 * @param concerts - 공연 리스트
	 * @param totalElements - 전체 공연 개수
	 * @param totalPages - 전체 페이지수
	 * @param currentPage - 현재 페이지수
	 * @param currentSize - 현재 페이지의 공연 개수
	 */
	public record GetConcerts(
		List<ConcertInfo.GetConcertListDto> concerts,
		long totalElements,
		int totalPages,
		int currentPage,
		int currentSize
	) {
		public static GetConcerts from(Page<ConcertInfo.GetConcertListDto> concertPage) {
			return new GetConcerts(
				concertPage.getContent(),
				concertPage.getTotalElements(),
				concertPage.getTotalPages(),
				concertPage.getNumber() + 1,
				concertPage.getNumberOfElements()
			);
		}
	}

	/**
	 *
	 * @param concertDates - 공연일정 리스트
	 * @param totalElements - 전체 공연일정 개수
	 * @param totalPages - 전체 페이지수
	 * @param currentPage - 현재 페이지수
	 * @param currentSize - 현재 페이지의 공연일정 개수
	 */
	public record GetAvailableConcertDates(
		List<ConcertInfo.GetConcertDateListDto> concertDates,
		long totalElements,
		int totalPages,
		int currentPage,
		int currentSize
	) {
		public static GetAvailableConcertDates from(Page<ConcertInfo.GetConcertDateListDto> concertDatePage) { // from절에는 페이징처리 결과를 넣으면 된다.

			return new GetAvailableConcertDates(
				concertDatePage.getContent(),
				concertDatePage.getTotalElements(),
				concertDatePage.getTotalPages(),
				concertDatePage.getNumber() + 1,
				concertDatePage.getNumberOfElements()
			);
		}
	}
}
