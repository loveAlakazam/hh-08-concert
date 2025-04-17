package io.hhplus.concert.domain.user;

import java.util.UUID;

import io.hhplus.concert.domain.user.User;

public interface UserRepository {
    User save(User user);
    User findById(long id);

}
