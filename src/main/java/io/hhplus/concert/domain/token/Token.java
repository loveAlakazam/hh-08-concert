package io.hhplus.concert.domain.token;

import static io.hhplus.concert.interfaces.api.common.validators.DateValidator.*;
import static io.hhplus.concert.interfaces.api.token.TokenErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 토큰 PK

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.WAITING; // 토큰 상태

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 토큰 만료일자

    @Column(name="uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID uuid; // 유저의 UUID


    /**
     * 연관관계
     */
    // 토큰:유저=1:1
    @OneToOne
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
        nullable = false,
        unique = true
    )
    private User user;

    // 정적팩토리 메소드
    @Builder
    private Token(User user, UUID uuid) {
        this.user = user;
        this.uuid = uuid;
    }
    public static Token of (User user, UUID uuid) {
        if(user == null) throw new BusinessException(NOT_NULLABLE);
        if(uuid == null) throw new BusinessException(NOT_NULLABLE);

        return Token.builder()
            .user(user)
            .uuid(uuid)
            .build();
    }
    /**
     * 토큰 활성화
     */
    public void activate() {
        // 이미 활성화된 토큰은 활성화 처리가 불가능하다
        if(isActivated()) throw new BusinessException(INVALID_ACCESS);
        // 토큰 만료됐는지 확인
        if(this.isExpiredToken()) throw new BusinessException(EXPIRED_OR_UNAVAILABLE_TOKEN);

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

    public void expire(LocalDateTime expiredAt) {
        if(isPastDateTime(expiredAt))
            this.expiredAt =  expiredAt;
    }

    /**
     * 만료일자가 이미 지난 일자인지 확인
     * @return boolean
     */
    public boolean isExpiredToken() {
        return isPastDateTime(this.expiredAt);
    }

    /**
     * 대기상태 토큰으로 토큰을 발급한다.
     * 큐에 진입하게되면
     */
    public void issue(User user) {
        // 이미 활성화된 토큰은 대기상태로 전환이 불가능하다
        if(isActivated()) throw new BusinessException(INVALID_ACCESS);

        // 대기토큰을 생성한다
        if(user == null) throw new BusinessException(NOT_NULLABLE);

        // 상태 초기화
        this.status = TokenStatus.WAITING;

        // 토큰만료일자 초기화
        LocalDateTime now = LocalDateTime.now();
        this.expiredAt = now.plusMinutes(VALID_TOKEN_DURATION_MINUTE_UNIT);
    }

    // 비즈니스 정책
    public static final int VALID_TOKEN_DURATION_MINUTE_UNIT = 5; // 5분 - 토큰 유효기간 분단위
    public static final int VALID_TOKEN_DURATION_SECOND_UNIT = 60 * VALID_TOKEN_DURATION_MINUTE_UNIT; // 토큰 유효기간 초단위

}
