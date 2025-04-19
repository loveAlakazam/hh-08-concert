package io.hhplus.concert.domain.concert;

import java.util.List;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertMaintenanceService {
	private final ConcertDateRepository concertDateRepository;
	private final ConcertSeatRepository concertSeatRepository;

	public void deletePastConcertDates() {
		// 일정이 지난 날짜를 구한다
		List<Long> finishedConcertDateIds = concertDateRepository.findFinishedConcertDates();

		for(long concertDateId: finishedConcertDateIds) {
			// 콘서트 좌석 삭제
			concertSeatRepository.deleteConcertSeatByConcertDateId(concertDateId);
			// 콘서트 일정 삭제
			concertDateRepository.deleteConcertDate(concertDateId);
		}

	}
}
