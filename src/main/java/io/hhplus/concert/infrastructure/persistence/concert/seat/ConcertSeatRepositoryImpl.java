package io.hhplus.concert.infrastructure.persistence.concert.seat;


import java.util.List;

import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.concert.repository.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {
    private final ConcertSeatJpaRepository concertSeatJpaRepository;

    @Override
    public List<ConcertSeat> findConcertSeats(long concertId, long concertDateId) {
        return concertSeatJpaRepository.findAllSeats(concertId, concertDateId);
    }
}
