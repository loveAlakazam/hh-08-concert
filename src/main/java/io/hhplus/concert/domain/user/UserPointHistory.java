package io.hhplus.concert.domain.user;

import io.hhplus.concert.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "user_point_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
     * 연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_point_id",
        nullable = false,
        foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private UserPoint userPoint;


    /**
     * 정적팩토리메소드
     */
    @Builder
    private UserPointHistory(
        UserPoint userPoint,
        long amount,
        UserPointHistoryStatus status
    ) {
        this.amount = amount;
        this.status = status;
        this.userPoint = userPoint;
    }

    /**
     * 정적 팩토리 메소드로 포인트히스토리 추가
     * @param user
     * @param amount
     * @param status
     * @return UserPointHistory
     */
    public static UserPointHistory of(
        UserPoint userPoint,
        long amount,
        UserPointHistoryStatus status
    ) {
        return UserPointHistory
            .builder()
            .amount(amount)
            .status(status)
            .userPoint(userPoint)
            .build();
    }
}

