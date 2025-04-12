package io.hhplus.concert.domain.concert;

import java.util.List;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse;

public interface ConcertSeatRepository {
	List<ConcertSeat> findConcertSeats(long concertId, long concertDateId);
	ConcertSeatDetailResponse getConcertSeatInfo(long id);
	ConcertSeat findConcertSeatById(long id);
	ConcertSeat saveOrUpdate(ConcertSeat concertSeat);
}
