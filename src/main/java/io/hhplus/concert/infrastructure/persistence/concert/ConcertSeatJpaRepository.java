package io.hhplus.concert.infrastructure.persistence.concert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.reservation.ReservationStatus;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
	@Query(
		"""
		SELECT cs 
		FROM ConcertSeat cs
			JOIN FETCH cs.concert c
			JOIN FETCH cs.concertDate cd
		WHERE  
			c.id = :concertId
			AND cd.id = :concertDateId 
			AND c.deleted = false
			AND cd.deleted = false
			AND cs.deleted = false
		"""
	)
	List<ConcertSeat> findAllSeats(@Param("concertId") Long concertId, @Param("concertDateId") Long concertDateId);

	@Query("""
  		SELECT cs
  		FROM ConcertSeat cs
  			JOIN FETCH cs.concertDate  cd
  			JOIN FETCH cs.concert c
  		WHERE 
  			cs.deleted = false
  			AND cd.deleted = false
  			AND cd.isAvailable = true
  			AND cs.id = :concertSeatId
	""")
	Optional<ConcertSeat> getConcertSeatInfo(@Param("concertSeatId") Long concertSeatId);

	@Modifying
	@Query(value = """
		UPDATE concert_seats cs
		JOIN concert_dates cd ON cd.id = cs.concert_date_id
		SET cs.deleted = 1
		WHERE cs.deleted = 0
			AND cd.deleted = 0
			AND cd.id = :id
	""", nativeQuery = true)
	void softDeleteConcertSeat(@Param("id") long concertDateId);

	@Query("""
		SELECT cs
		FROM ConcertSeat cs
		WHERE cs.deleted = false
			AND cs.concertDate.id = :concertDateId
	""")
	List<ConcertSeat> findConcertSeatsByConcertDateId(@Param("concertDateId") long pastConcertDateId);
}
