package io.hhplus.concert.domain.token;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    public Token(TokenStatus status, LocalDateTime expiredAt) {
        super();
        this.status = status;
        this.expiredAt = expiredAt;
    }
    public Token(long id, TokenStatus status, LocalDateTime expiredAt) {
        this(status, expiredAt);
        this.id = id;
    }


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
    /**
     * 토큰 활성화
     */
    public void activate() {
        // 상태변경: WAITING -> ACTIVE
        this.status = TokenStatus.ACTIVE;

        // 토큰만료일자 다시 갱신
        LocalDateTime now = LocalDateTime.now();
        this.expiredAt = now.plusMinutes(VALID_TOKEN_DURATION_MINUTE_UNIT);
    }
    public boolean isActivated() {
        // 활성상태인
        if(this.status != TokenStatus.ACTIVE) return false;

        // 유효기간이 만료되면 활성상태가 아니다.
        if(this.isExpiredToken()) return false;
        return true;
    }

    /**
     * 만료일자가 이미 지난 일자인지 확인
     * @return boolean
     */
    public boolean isExpiredToken() {
       return isPastDateTime(this.expiredAt);
    }

    /**
     * 토큰 발급
     */
    public static Token issuerFor(User user) {
        Token token = new Token();
        LocalDateTime now = LocalDateTime.now();
        token.setUser(user);
        token.setStatus(TokenStatus.WAITING);
        token.setExpiredAt(now.plusMinutes(VALID_TOKEN_DURATION_MINUTE_UNIT));
        return token;
    }

}
