package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.ConcertSeat;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
	@Query(
		"""
		SELECT cs 
		FROM Concert c
			JOIN FETCH c.dates cd
			JOIN FETCH c.seats cs
		WHERE  
			c.deleted = false
			AND cd.deleted = false
			AND cs.deleted = false
			AND c.id = :concertId
			AND cd.id = :concertDateId 
		"""
	)
	List<ConcertSeat> findAllSeats(@Param("concertId") Long concertId, @Param("concertDateId") Long concertDateId);

	@Query("""
  		SELECT cs
  		FROM ConcertSeat cs
  			JOIN FETCH cs.date  cd
  		WHERE 
  			cd.deleted = false
  			AND cd.available = false
  			AND cs.deleted = false
  			AND cs.id = :concertSeatId
	""")
	Optional<ConcertSeat> getConcertSeatInfo(@Param("concertSeatId") Long concertSeatId);

	@Modifying
	@Query("""
		UPDATE ConcertSeat cs
		SET cs.deleted = true
		WHERE cs.deleted = false
			AND cs.concertDate.deleted = false
			AND cs.concertDate.id = :id
	""")
	void softDeleteConcertSeat(@Param("id") long concertDateId);
}
