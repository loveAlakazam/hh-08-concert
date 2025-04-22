package io.hhplus.concert.domain.reservation;

public interface ReservationScheduler {
	// 매일 0시에 임시예약기간이 만료된 예약들은 soft-deleted 처리된다
	void deleteExpiredReservations();

	// 임시예약의 유효일자가 지났다면 해당예약은 자동으로 예약취소 처리가된다 (5분주기)
	// 임시예약의 유효시간이 지났고 해당좌석이 아직 확정되지 않았다면 해당좌석의 상태를 예약가능으로 변경된다
	void cancel();
}
