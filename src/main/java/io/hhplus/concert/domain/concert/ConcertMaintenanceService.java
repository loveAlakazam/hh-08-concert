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

	/**
	 * 현재기준으로 콘서트일정이 이미 지난날짜이면 soft-delete 한다.
	 */
	public void deletePastConcertDates() {
		// 일정이 지난 공연일정의 아이디를 구한다
		List<Long> finishedConcertDateIds = concertDateRepository.findFinishedConcertDateIds();

		for(long concertDateId: finishedConcertDateIds) {
			// 콘서트 좌석 삭제
			concertSeatRepository.deleteConcertSeatByConcertDateId(concertDateId);
			// 콘서트 일정 삭제
			concertDateRepository.deleteConcertDate(concertDateId);
		}
	}
	/**
	 * 해당 공연일정의 좌석50개 전부가 모두 예약상태(임시예약+예약확정) 라면 매진상태이므로
	 * 해당 공연일정의 에약가능여부를 false 로 변경한다.
	 */
	public void checkSoldOut() {
		// 좌석 콘서트의 일정별로 좌석의 개수를 확인한다.
		List<ConcertDate> concertDates = concertDateRepository.findAllNotDeleted();

		for(ConcertDate concertDate : concertDates) {
			// 콘서트좌석에서 예약가능한 좌석개수를 확인한다.
			int numberOfAvailableSeat = concertDate.countAvailableSeats();

			// 예약가능한 좌석개수에 따라 공연일정의 isAvailable 값이 변경된다.
            if (numberOfAvailableSeat == 0) {
                concertDate.soldOut();
            } else {
				concertDate.available();
			}
			// 데이터베이스 저장
			concertDateRepository.save(concertDate);
		}
	}
}
