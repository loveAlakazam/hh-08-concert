package io.hhplus.concert.infrastructure.persistence.user;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository {
	private final UserPointJpaRepository userPointJpaRepository;

	@Override
	public UserPoint findByUserId(long id) {
		return userPointJpaRepository.findByUserId(id).orElse(null);
	}

	@Override
	public UserPoint save(UserPoint userPoint) {
		return userPointJpaRepository.save(userPoint);
	}

	@Override
	public void deleteAll() {
		userPointJpaRepository.deleteAll();
	}
}
