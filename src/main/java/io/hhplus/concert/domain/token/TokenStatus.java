package io.hhplus.concert.domain.token;

public enum TokenStatus {
    ACTIVE, // 활성상태 토큰 (서비스 접근 가능)
    WAITING // 대기상태 토큰 (서비스 접근 불가능)
}
