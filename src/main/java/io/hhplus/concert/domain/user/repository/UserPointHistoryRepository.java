package io.hhplus.concert.domain.user.repository;

import io.hhplus.concert.domain.user.entity.PointHistoryStatus;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.entity.UserPointHistory;

public interface UserPointHistoryRepository {
	UserPointHistory save(long amount, PointHistoryStatus status , User user);
}
