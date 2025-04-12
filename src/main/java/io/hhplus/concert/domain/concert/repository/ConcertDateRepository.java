package io.hhplus.concert.domain.concert.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.entity.ConcertDate;

public interface ConcertDateRepository {
	Page<ConcertDate> findAll(long concertId, Pageable pageable);
	ConcertDate findConcertDateById(long id);
}
