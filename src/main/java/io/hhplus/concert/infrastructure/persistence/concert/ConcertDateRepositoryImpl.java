package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertDateRepositoryImpl implements ConcertDateRepository {
    private final ConcertDateJpaRepository concertDateJpaRepository;

    @Override
    public Page<ConcertDate> findAll(long concertId, Pageable pageable) {
        return concertDateJpaRepository.findUpcomingConcertDates(concertId, LocalDate.now(), pageable);
    }

    @Override
    public ConcertDate findConcertDateById(long id) {
        return concertDateJpaRepository.findById(id).orElse(null);
    }

    @Override
    public List<Long> findFinishedConcertDates() {
        return concertDateJpaRepository.findFinishedConcertDates(LocalDate.now());
    }

    @Override
    public void deleteConcertDate(long concertDateId) {
        concertDateJpaRepository.softDeleteConcertDate(concertDateId, LocalDate.now());
    }
}
