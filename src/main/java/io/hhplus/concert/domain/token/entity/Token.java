package io.hhplus.concert.domain.token.entity;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "tokens")
@RequiredArgsConstructor
public class Token extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 토큰 PK

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.WAITING; // 토큰 상태

    @Column(name = "valid_duration", nullable = false)
    private long validDuration; // 토큰 유효 시간(초 단위)

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 토큰 만료시간

    @Column(name= "position", nullable = false)
    private int position; // 대기 순서

    /**
     * 연관관계
     */
    // 토큰:유저=1:1
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 비즈니스 정책
    // 비즈니스 책임

}
