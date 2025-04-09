package io.hhplus.concert.domain.concert.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.entity.Concert;

public interface ConcertRepository {
	List<Concert> findAll();
	Page<Concert> findAll(Pageable pageable);
}
