package io.hhplus.concert.infrastructure.persistence.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.repository.ConcertRepository;
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
}
