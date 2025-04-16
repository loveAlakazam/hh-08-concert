package io.hhplus.concert.infrastructure.persistence.reservation;

import java.util.Optional;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.reservation.ReservationResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

	@Query("""
		SELECT r.*
		FROM Reservation r
			LEFT JOIN r.user u 
			LEFT JOIN r.concert c
			LEFT JOIN r.concertDate cd
			LEFT JOIN r.concertSeat cs
		WHERE r.deleted = false
			AND cd.deleted = false
			AND cd.isAvailable = true
			AND c.id = :concertId
			AND cd.id = :concertDateId
			AND cs.id = :concertSeatId 
	""")
	Optional<Reservation> getReservationDetailInfo(
		@Param("concertId") long concertId,
		@Param("concertDateId") long concertDateId,
		@Param("concertSeatId") long concertSeatId
	);

	@Query("""
		SELECT r.*
		FROM Reservation r
			LEFT JOIN r.user u
			LEFT JOIN r.concert c
			LEFT JOIN r.concertDate cd
			LEFT JOIN r.concertSeat cs
		WHERE r.deleted = false
			AND cd.isAvailable = true
			AND u.id = :userId
			AND cs.id = :concertSeatId
	""")
	Optional<Reservation> findByConcertSeatIdAndUserId(@Param("userId") long userId, @Param("concertSeatId") long concertSeatId);
}
