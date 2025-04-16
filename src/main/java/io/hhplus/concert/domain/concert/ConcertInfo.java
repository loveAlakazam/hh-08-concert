package io.hhplus.concert.domain.concert;

import java.util.List;

import org.springframework.data.domain.Page;

public class ConcertInfo {
	public record GetConcertList(Page<Concert> concertPage) {
		public static GetConcertList from(Page<Concert> concertPage) {
			return new GetConcertList(concertPage);
		}
	}
	public record GetConcertDateList(Page<ConcertDate> concertDatePage) {
		public static GetConcertDateList from(Page<ConcertDate> concertDatePage) {
			return new GetConcertDateList(concertDatePage);
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
