package io.hhplus.concert.domain.user.repository;

import java.util.Optional;

import io.hhplus.concert.domain.user.entity.User;

public interface UserRepository {
    User save(User user);
    User findById(long id);
}
