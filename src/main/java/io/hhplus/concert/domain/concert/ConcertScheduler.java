package io.hhplus.concert.domain.concert;

public interface ConcertScheduler {
	// 매일 0시에 지난 공연일정과 관련된 좌석들은 soft-delete 삭제된다
	void deletePastConcertDates();

	// 매일 0시에 어제랭킹결과를 데이터베이스에 저장한다
	void saveYesterdayDailyRanking();

	// 매일 0시에 오늘기준 6일전~1일전 데이터를 snapshot으로부터 가져와서 redis에 반영한다
	void loadWeeklyBaseRankingFromSnapshots();
}
