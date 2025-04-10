package io.hhplus.concert.infrastructure.persistence.concert.seat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
	@Query(
		"""
		SELECT cs.* 
		FROM Concert c
			JOIN FETCH c.dates cd
			JOIN FETCH c.seats cs
		WHERE  c.id = :concertId
			AND cd.isAvailable = true
			AND cd.id = :concertDateId 
		"""
	)
	List<ConcertSeat> findAllSeats(@Param("concertId") Long concertId, @Param("concertDateId") Long concertDateId);

	@Query("""
  		SELECT new io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse(
  			cs.id,
  			cs.number,
  			cs.price,
  			cs.isAvailable,
  			c.id,
  			cd.id
  		)
  		FROM ConcertSeats cs
  			JOIN FETCH c.dates cd
  			JOIN FETCH c.seats cs
  		WHERE cs.id = :concertSeatId
  			AND cd.isAvailable = true
		""")
	Optional<ConcertSeatDetailResponse> getConcertSeatInfo(@Param("concertSeatId") Long concertSeatId);
}
