package io.hhplus.concert.infrastructure.persistence.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public ConcertInfo.GetConcertList findAll() {
        List<Concert> concerts = concertJpaRepository.findAll();
        return ConcertInfo.GetConcertList.from(concerts);
    }

    @Override
    public Concert findById(long id) {
        return concertJpaRepository.findById(id).orElse(null);
    }
    @Override
    public Concert saveOrUpdate(Concert concert) {
        return concertJpaRepository.save(concert);
    }

    @Override
    public void deleteAll() {
        concertJpaRepository.deleteAll();
    }
}
