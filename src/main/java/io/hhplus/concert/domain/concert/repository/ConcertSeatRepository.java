package io.hhplus.concert.domain.concert.repository;

import java.util.List;

import io.hhplus.concert.domain.concert.entity.ConcertSeat;

public interface ConcertSeatRepository {
	List<ConcertSeat> findConcertSeats(long concertId, long concertDateId);
}
