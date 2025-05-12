package io.hhplus.concert.infrastructure.persistence.concert;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {
    private final ConcertSeatJpaRepository concertSeatJpaRepository;

    @Override
    public ConcertInfo.GetConcertSeatList findConcertSeats(long concertId, long concertDateId) {
        List<ConcertSeat> concertSeats =  concertSeatJpaRepository.findAllSeats(concertId, concertDateId);
        return ConcertInfo.GetConcertSeatList.from(concertSeats);
    }

    @Override
    public ConcertSeat getConcertSeatInfo(long concertSeatId) {
        return concertSeatJpaRepository.getConcertSeatInfo(concertSeatId).orElse(null);
    }

    @Override
    public Optional<ConcertSeat> findById(long id) {
        return concertSeatJpaRepository.findById(id);
    }

    @Override
    public ConcertSeat saveOrUpdate(ConcertSeat concertSeat) {
        return concertSeatJpaRepository.save(concertSeat);
    }

    @Override
    public void deleteConcertSeatByConcertDateId(long concertDateId) {
        concertSeatJpaRepository.softDeleteConcertSeat(concertDateId);
    }

    @Override
    public void deleteAll() {
        concertSeatJpaRepository.deleteAll();
    }

    @Override
    public List<ConcertSeat> findByConcertDateId(long pastConcertDateId) {
        return concertSeatJpaRepository.findConcertSeatsByConcertDateId(pastConcertDateId);
    }
}
