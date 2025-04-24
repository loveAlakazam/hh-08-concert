package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import lombok.RequiredArgsConstructor;
@Repository
@RequiredArgsConstructor
public class ConcertDateRepositoryImpl implements ConcertDateRepository {
    private final ConcertDateJpaRepository concertDateJpaRepository;

    @Override
    public List<ConcertDate> findAllAvailable(long concertId) {
        return concertDateJpaRepository.findUpcomingConcertDates(concertId, LocalDate.now());
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

    @Override
    public ConcertDate save(ConcertDate concertDate) {
        return concertDateJpaRepository.save(concertDate);
    }

    @Override
    public void deleteAll() {
        concertDateJpaRepository.deleteAll();
    }

    @Override
    public List<ConcertDate> findAll() {
        return List.of();
    }
}
