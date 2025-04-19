package io.hhplus.concert.domain.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.ConcertDate;

public interface ConcertDateRepository {
	List<ConcertDate> findAll(long concertId);
	ConcertDate findConcertDateById(long id);

	List<Long> findFinishedConcertDates();
	void deleteConcertDate(long concertDateId);
	ConcertDate save(ConcertDate concertDate);

	void deleteAll();
}
