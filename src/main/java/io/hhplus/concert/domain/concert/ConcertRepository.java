package io.hhplus.concert.domain.concert;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.concert.Concert;

public interface ConcertRepository {
	ConcertInfo.GetConcertList findAll();
	Concert findById(long id);
	Concert saveOrUpdate(Concert concert);

	void deleteAll();
}
