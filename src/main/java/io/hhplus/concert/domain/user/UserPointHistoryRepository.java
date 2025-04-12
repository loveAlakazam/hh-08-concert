package io.hhplus.concert.domain.user;

public interface UserPointHistoryRepository {
	UserPointHistory save(long amount, UserPointHistoryStatus status , User user);
}
