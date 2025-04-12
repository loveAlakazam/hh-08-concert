package io.hhplus.concert.domain.user;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.hhplus.concert.domain.user.User.*;
import static io.hhplus.concert.domain.user.UserExceptionMessage.*;
import static io.hhplus.concert.domain.user.UserPointHistoryExceptionMessage.*;

@Entity
@Getter
@Table(name = "user_point_histories")
@RequiredArgsConstructor
public class UserPointHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 포인트 내역 PK

    @Column(name = "amount", nullable = false)
    private long amount; // 포인트 내역 기록 금액

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserPointHistoryStatus status; // 포인트 내역 상태

    /**
     * 생성자
     */
    public UserPointHistory(long amount, UserPointHistoryStatus status, User user) {
        super();
        // 유저검증
        if(user == null) throw new InvalidValidationException(SHOULD_NOT_EMPTY);

        // 충전/사용 금액 검증
        validateAmount(amount);
        switch(status) {
            case USE -> {
                if(user.getCurrentPoint() < amount) throw new InvalidValidationException(LACK_OF_YOUR_POINT);
            }
            case CHARGE -> {
                if(amount < CHARGE_POINT_MINIMUM) throw new InvalidValidationException(CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM);
                if(amount > CHARGE_POINT_MAXIMUM) throw new InvalidValidationException(CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM);
            }
            default -> throw new InvalidValidationException(INVALID_POINT_STATUS);
        }



        this.amount = amount;
        this.status = status;
        this.user = user;
    }


    /**
     * 연관관계
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 포인트내역:유저=N:1

    // 비즈니스 정책
    // 비즈니스 책임
    public static void validateAmount(long amount) {
        // 포인트 충전/사용 내역 기록 금액 검증
        if(amount <= 0) throw new InvalidValidationException(AMOUNT_SHOULD_BE_POSITIVE_NUMBER);
    }


}

