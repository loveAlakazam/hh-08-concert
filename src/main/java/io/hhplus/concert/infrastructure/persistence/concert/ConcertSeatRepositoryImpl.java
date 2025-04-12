package io.hhplus.concert.infrastructure.persistence.concert;


import java.util.List;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {
    private final ConcertSeatJpaRepository concertSeatJpaRepository;

    @Override
    public List<ConcertSeat> findConcertSeats(long concertId, long concertDateId) {
        return concertSeatJpaRepository.findAllSeats(concertId, concertDateId);
    }

    @Override
    public ConcertSeatDetailResponse getConcertSeatInfo(long concertSeatId) {
        return concertSeatJpaRepository.getConcertSeatInfo(concertSeatId).orElse(null);
    }

    @Override
    public ConcertSeat findConcertSeatById(long id) {
        return concertSeatJpaRepository.findById(id).orElse(null);
    }

    @Override
    public ConcertSeat saveOrUpdate(ConcertSeat concertSeat) {
        return concertSeatJpaRepository.save(concertSeat);
    }
}
