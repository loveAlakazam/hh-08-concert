package io.hhplus.concert.domain.concert;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

public class ConcertInfo {
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
	public record GetConcertList(List<GetConcertListDto> concerts, int size) {
		public static GetConcertList from(List<Concert> concerts) {
			List<GetConcertListDto> concertListDtos = concerts.stream().map(GetConcertListDto::from).toList();
			return new GetConcertList(concertListDtos, concertListDtos.size());
		}
	}
	public record GetConcertDateList(List<ConcertDate> concertDates, int size) {
		public static GetConcertDateList from(List<ConcertDate> concertDates) {
			return new GetConcertDateList(concertDates, concertDates.size());
		}
	}
	public record GetConcertSeatList(List<ConcertSeat> concertSeatList) {
		public static GetConcertSeatList from(List<ConcertSeat> concertSeatList) {
			return new GetConcertSeatList(concertSeatList);
		}
	}
	public record GetConcertSeat(ConcertSeat concertSeat) {
		public static GetConcertSeat from(ConcertSeat concert) {
			return new GetConcertSeat(concert);
		}
	}
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
