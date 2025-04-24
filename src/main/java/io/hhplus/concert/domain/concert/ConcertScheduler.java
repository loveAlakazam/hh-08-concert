package io.hhplus.concert.domain.concert;

public interface ConcertScheduler {
	// 매일 0시에 지난 공연일정과 관련된 좌석들은 soft-delete 삭제된다
	void deletePastConcertDates();
	// 1시간 주기로 매진 상태인지 아닌지 체크후 공연일정상태를 변경하는 스케줄러
	void checkSoldOut();
}
