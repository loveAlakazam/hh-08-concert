package io.hhplus.concert.infrastructure.persistence.concert.seat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.entity.ConcertSeat;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
	@Query("SELECT cs.* FROM Concert c "+
		" JOIN FETCH c.dates cd " +
		" JOIN FETCH c.seats cs " +
		" WHERE  c.id = :concertId "+
		" AND cd.isAvailable = true " +
		" AND cd.id = :concertDateId "
	)
	List<ConcertSeat> findAllSeats(@Param("concertId") Long concertId, @Param("concertDateId") Long concertDateId);
}
