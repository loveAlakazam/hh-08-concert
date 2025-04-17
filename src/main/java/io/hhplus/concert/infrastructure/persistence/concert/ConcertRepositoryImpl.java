package io.hhplus.concert.infrastructure.persistence.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }
    @Override
    public Page<Concert> findAll(Pageable pageable) {
        return concertJpaRepository.findAll(pageable);
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
