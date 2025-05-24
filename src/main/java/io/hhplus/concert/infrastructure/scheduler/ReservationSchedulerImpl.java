package io.hhplus.concert.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.application.usecase.reservation.ReservationMaintenanceUsecase;
import io.hhplus.concert.domain.reservation.ReservationScheduler;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationSchedulerImpl implements ReservationScheduler {
	private final ReservationMaintenanceUsecase reservationMaintenanceUsecase;

	@Scheduled(cron = "0 0 0 * * *")
	@Override
	public void deleteCanceledReservations() {
		reservationMaintenanceUsecase.deleteCanceledReservations();
	}

	@Scheduled(cron = "0 */5 0 * * * ")
	@Override
	public void cancel() {
		reservationMaintenanceUsecase.cancel();
	}

}
