package io.hhplus.concert.domain.user;

public interface UserPointHistoryRepository {
	UserPointHistory save(UserPointHistory userPointHistory);

	void deleteAll();
}
