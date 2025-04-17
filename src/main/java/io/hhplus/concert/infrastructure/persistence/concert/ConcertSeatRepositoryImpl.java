package io.hhplus.concert.infrastructure.persistence.concert;


import java.util.List;
import java.util.Optional;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {
    private final ConcertSeatJpaRepository concertSeatJpaRepository;

    @Override
    public List<ConcertSeat> findConcertSeats(long concertId, long concertDateId) {
        return concertSeatJpaRepository.findAllSeats(concertId, concertDateId);
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
}
