package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.ConcertDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, Long> {
	@Query(
		"""
		SELECT cd.* 
		FROM Concert c 
			JOIN FETCH c.dates cd
		WHERE cd.deleted = false
			AND cd.isAvailable = true
			AND cd.progressDate >= :currentDate
  			AND c.id = :concertId
		"""
	)
	Page<ConcertDate>  findUpcomingConcertDates (@Param("concertId") Long concertId, @Param("currentDate") LocalDate currentDate, Pageable pageable);
}
