package io.hhplus.concert.infrastructure.persistence.user;


import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Repository
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

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }
}
