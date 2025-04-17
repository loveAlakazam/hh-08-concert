package io.hhplus.concert.infrastructure.persistence.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public Concert findById(long id) {
        return concertJpaRepository.findById(id).orElse(null);
    }
    @Override
    public Concert saveOrUpdate(Concert concert) {
        return concertJpaRepository.save(concert);
    }
}
