package io.hhplus.concert.infrastructure.persistence.user;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.user.UserPointHistory;
import io.hhplus.concert.domain.user.UserPointHistoryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserPointHistoryRepositoryImpl implements UserPointHistoryRepository {
	private final UserPointHistoryJpaRepository userPointHistoryJpaRepository;

	@Override
	public UserPointHistory save(UserPointHistory userPointHistory) {
		return userPointHistoryJpaRepository.save(userPointHistory);
	}

	@Override
	public void deleteAll() {
		userPointHistoryJpaRepository.deleteAll();
	}
}
