package io.hhplus.concert.domain.token.service;

import io.hhplus.concert.domain.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    // 토큰발급 요청
    // 대기번호 조회
    // 토큰 상태변경
}
