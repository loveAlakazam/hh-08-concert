package io.hhplus.concert.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.concert.ConcertMaintenanceService;
import io.hhplus.concert.domain.concert.ConcertScheduler;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConcertSchedulerImpl implements ConcertScheduler {
	private final ConcertMaintenanceService concertMaintenanceService;

	@Scheduled(cron = "0 0 0 * * *")
	@Override
	public void deletePastConcertDates() {
		concertMaintenanceService.deletePastConcertDates();
	}

	@Scheduled(cron = "0 0 0 * * *")
	@Override
	public void saveYesterdayDailyRanking() {
		concertMaintenanceService.saveDailySnapshot();
	}
}
