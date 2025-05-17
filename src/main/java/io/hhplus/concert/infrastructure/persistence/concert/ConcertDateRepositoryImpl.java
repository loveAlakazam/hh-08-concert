package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertInfo;
import lombok.RequiredArgsConstructor;
@Repository
@RequiredArgsConstructor
public class ConcertDateRepositoryImpl implements ConcertDateRepository {
    private final ConcertDateJpaRepository concertDateJpaRepository;

    @Override
    public ConcertInfo.GetConcertDateList findAllAvailable(long concertId) {
        List<ConcertDate> concertDates = concertDateJpaRepository.findUpcomingConcertDates(concertId, LocalDate.now());
        return ConcertInfo.GetConcertDateList.from(concertDates);
    }

    @Override
    public ConcertDate findConcertDateById(long id) {
        return concertDateJpaRepository.findById(id).orElse(null);
    }

    @Override
    public ConcertDate findConcertDateByIdAndNotDeleted(long id) {
        return concertDateJpaRepository.findByIdAndNotDeleted(id).orElse(null);
    }

    @Override
    public List<Long> findFinishedConcertDateIds() {
        return concertDateJpaRepository.findFinishedConcertDateIds(LocalDate.now());
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
    public List<ConcertDate> findAllNotDeleted() {
        return concertDateJpaRepository.findAllNotDeleted();
    }
}
