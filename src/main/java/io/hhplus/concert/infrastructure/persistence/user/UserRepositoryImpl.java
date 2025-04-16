package io.hhplus.concert.infrastructure.persistence.user;


import java.util.UUID;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User findById(long id) {
        return  userJpaRepository.findById(id).orElse(null);
    }
}
