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

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 토큰 만료일자


    /**
     * 연관관계
     */
    // 토큰:유저=1:1
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    // 비즈니스 정책
    public static final int VALID_TOKEN_DURATION_MINUTE_UNIT = 30; // 30분 - 토큰 유효기간 분단위
    public static final int VALID_TOKEN_DURATION_SECOND_UNIT = 60 * VALID_TOKEN_DURATION_MINUTE_UNIT; // 토큰 유효기간 초단위

    // 비즈니스 책임

}
