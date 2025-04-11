package io.hhplus.concert.infrastructure.persistence.user;


import java.util.UUID;

import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.domain.user.repository.UserRepository;
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

    @Override
    public User findByUUID(UUID uuid) {
        return userJpaRepository.findByUUID(uuid).orElse(null);
    }
}
