package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.ConcertDate;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, Long> {
	@Query(
		"""
		SELECT cd
		FROM ConcertDate cd 
			JOIN FETCH cd.concert c	
			JOIN FETCH cd.seats cs		
		WHERE cd.deleted = false
			AND cd.isAvailable = true
			AND cd.progressDate >= :currentDate
  			AND c.id = :concertId
		"""
	)
	List<ConcertDate> findUpcomingConcertDates (@Param("concertId") Long concertId, @Param("currentDate") LocalDate currentDate);

	@Modifying
	@Query("""
		UPDATE ConcertDate cd
		SET cd.deleted = true
		WHERE cd.deleted = false
			AND cd.progressDate < :now
			AND cd.id = :id
	""")
	void softDeleteConcertDate(@Param("id") long concertDateId, @Param("now") LocalDate now);


	@Query("""
		SELECT cd.id
		FROM ConcertDate cd
		WHERE cd.deleted = false 
			AND cd.progressDate < :now 	
	""")
	List<Long> findFinishedConcertDateIds(@Param("now") LocalDate now);

	@Query("""
		SELECT cd
		FROM ConcertDate cd
			JOIN FETCH cd.seats cs
		WHERE cd.deleted = false
	""")
	List<ConcertDate> findAllNotDeleted();
}
