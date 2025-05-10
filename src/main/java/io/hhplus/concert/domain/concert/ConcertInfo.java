package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import io.hhplus.concert.interfaces.api.common.BusinessException;

public class ConcertInfo {
	/* 콘서트목록조회 - GetConcertList */
	public record GetConcertList(List<GetConcertListDto> concerts, int size) {
		public static GetConcertList from(List<Concert> concerts) {
			List<GetConcertListDto> concertListDtos = concerts.stream().map(GetConcertListDto::from).toList();
			return new GetConcertList(concertListDtos, concertListDtos.size());
		}
	}
	public record GetConcertListDto(long id, String name, String artistName, LocalDateTime createdAt, boolean deleted) {
		public static GetConcertListDto from(Concert concert) {
			return new GetConcertListDto(
				concert.getId(),
				concert.getName(),
				concert.getArtistName(),
				concert.getCreatedAt(),
				concert.isDeleted()
			);
		}
	}
	/* 콘서트 일정 목록조회 - GetConcertDateList */
	public record GetConcertDateList(List<GetConcertDateListDto> concertDates, int size) {
		public static GetConcertDateList from(List<ConcertDate> concertDates) {
			List<GetConcertDateListDto> concertDateListDtos = concertDates.stream().map(GetConcertDateListDto::from).toList();
			return new GetConcertDateList(concertDateListDtos, concertDateListDtos.size());
		}
	}
	public record GetConcertDateListDto(
		long id,
		LocalDate progressDate,
		String place,
		long concertId,
		List<ConcertSeatListDto> concertSeats
	) {
		public static GetConcertDateListDto from(ConcertDate concertDate) {
			if(concertDate.getSeats() == null || concertDate.getConcert() == null)
				throw new BusinessException(NOT_NULLABLE);

			return new GetConcertDateListDto(
				concertDate.getId(),
				concertDate.getProgressDate(),
				concertDate.getPlace(),
				concertDate.getConcert().getId(),
				concertDate.getSeats().stream().map(ConcertSeatListDto::from).toList()
			);
		}
	}
	/* 콘서트 좌석 목록조회 - GetConcertSeatList */
	public record GetConcertSeatList(List<ConcertSeatListDto> concertSeatList) {
		public static GetConcertSeatList from(List<ConcertSeat> concertSeats) {
			List<ConcertSeatListDto> concertSeatListDtos = concertSeats.stream().map(ConcertSeatListDto::from).toList();
			return new GetConcertSeatList(concertSeatListDtos);
		}
	}
	public record ConcertSeatListDto(
		long id,
		int number,
		long price,
		boolean isAvailable,
		long concertId,
		long concertDateId,
		long version
	) {
		public static ConcertSeatListDto from(ConcertSeat concertSeat) {
			if(concertSeat.getConcert() == null || concertSeat.getConcertDate() == null)
				throw new BusinessException(NOT_NULLABLE);

			return new ConcertSeatListDto(
				concertSeat.getId(),
				concertSeat.getNumber(),
				concertSeat.getPrice(),
				concertSeat.isAvailable(),
				concertSeat.getConcert().getId(),
				concertSeat.getConcertDate().getId(),
				concertSeat.getVersion()
			);
		}
	}
	/* 콘서트 좌석 단건 조회 - GetConcertSeat */
	public record GetConcertSeat(ConcertSeat concertSeat) {
		public static GetConcertSeat from(ConcertSeat concert) {
			return new GetConcertSeat(concert);
		}
	}
	/* 콘서트 생성 - CreateConcert */
	public record CreateConcert(Concert concert) {
		public static CreateConcert from(Concert concert) {
			return new CreateConcert(concert);
		}
	}
	public record AddConcertDate(Concert concert) {
		public static AddConcertDate from(Concert concert) {
			return new AddConcertDate(concert);
		}
	}
}
