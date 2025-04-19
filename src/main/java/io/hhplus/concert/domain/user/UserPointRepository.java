package io.hhplus.concert.domain.user;


public interface UserPointRepository {

	UserPoint findByUserId(long id);
	UserPoint save(UserPoint userPoint);

	void deleteAll();
}
