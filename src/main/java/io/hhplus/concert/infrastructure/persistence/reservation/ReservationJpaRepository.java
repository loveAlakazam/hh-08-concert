package io.hhplus.concert.infrastructure.persistence.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.interfaces.api.reservation.ReservationResponse;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

	@Query("""
		SELECT r
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
		SELECT r
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

	@Modifying
	@Query("""
 		UPDATE Reservation r
 		SET r.deleted = true
 		WHERE r.deleted = false
 			AND r.status = :canceled
 			AND r.tempReservationExpiredAt < :now
	""")
	void deleteCanceledReservations(@Param("canceled") ReservationStatus canceled, @Param("now") LocalDateTime now);

	@Modifying
	@Query("""
		UPDATE Reservation r
		SET r.status = :canceled
		WHERE r.deleted = false
			AND r.status = :pendingPayment
			AND r.tempReservationExpiredAt < :now 
	""")
	void updateCanceledExpiredTempReservations(
		@Param("canceled") ReservationStatus canceled,
		@Param("pendingPayment") ReservationStatus pendingPayment,
		@Param("now") LocalDateTime now
	);

	@Query("""
		SELECT  r
		FROM Reservation r
		WHERE r.deleted = false
			AND r.status = :pendingPayment
			AND r.tempReservationExpiredAt < :now
	""")
	List<Reservation> findExpiredTempReservations(
		@Param("pendingPayment") ReservationStatus pendingPayment,
		@Param("now") LocalDateTime now
	);


	@Query("""
 		SELECT cs.id
 		FROM ConcertSeat cs
 			JOIN cs.reservation r
 		WHERE cs.deleted = false
 			AND r.deleted = false
 			AND r.status = :confirmed
	""")
	List<Long> findConfirmedConcertSeatIds(@Param("confirmed") ReservationStatus confirmed);
}
