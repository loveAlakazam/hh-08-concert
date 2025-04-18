package io.hhplus.concert.infrastructure.persistence.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.hhplus.concert.domain.user.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface UserJpaRepository extends JpaRepository<User, Long> { }
