package io.hhplus.concert.infrastructure.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.reservation.ReservationMaintenanceService;
import io.hhplus.concert.domain.reservation.ReservationScheduler;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationSchedulerImpl implements ReservationScheduler {
	private final ReservationMaintenanceService reservationMaintenanceService;

	@Scheduled(cron = "0 0 0 * * *")
	@Override
	public void deleteCanceledReservations() {
		reservationMaintenanceService.deleteCanceledReservations();
	}

	@Scheduled(cron = "0 */5 0 * * * ")
	@Override
	public void cancel() {
		reservationMaintenanceService.cancel();
	}

}
