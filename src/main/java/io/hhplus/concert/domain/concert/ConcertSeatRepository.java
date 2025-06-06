package io.hhplus.concert.domain.concert;

import java.util.List;
import java.util.Optional;


public interface ConcertSeatRepository {
	ConcertInfo.GetConcertSeatList findConcertSeats(long concertId, long concertDateId);
	ConcertSeat getConcertSeatInfo(long id);
	Optional<ConcertSeat> findById(long id);
	ConcertSeat saveOrUpdate(ConcertSeat concertSeat);

	void deleteConcertSeatByConcertDateId(long concertDateId);

	void deleteAll();

	List<ConcertSeat> findByConcertDateId(long pastConcertDateId);
}
