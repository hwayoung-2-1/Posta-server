package com.hwayoung.hwayoungserver.auth.service;

import com.hwayoung.hwayoungserver.auth.entity.AccessToken;
import com.hwayoung.hwayoungserver.auth.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {
    private final AccessTokenRepository accessTokenRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * 토큰을 DB에 저장
     */
    @Transactional
    public void saveToken(String token, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusSeconds(jwtExpiration / 1000);

        AccessToken accessToken = AccessToken.builder()
                .token(token)
                .userId(userId)
                .expiredAt(expiredAt)
                .createdAt(now)
                .build();

        accessTokenRepository.save(accessToken);
        log.info("토큰 저장 완료 - userId: {}, expiredAt: {}", userId, expiredAt);
    }

    /**
     * 토큰이 DB에 존재하고 만료되지 않았는지 검증
     */
    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        return accessTokenRepository.findValidToken(token, LocalDateTime.now()).isPresent();
    }

    /**
     * 만료된 토큰 삭제 (스케줄러에서 호출)
     */
    @Transactional
    public void deleteExpiredTokens() {
        accessTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * 이미 로그인 중인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isAlreadyLogin(Long user_id) {
        return accessTokenRepository.findLoggedinToken(user_id, LocalDateTime.now()).isPresent();
    }

    /**
     * 사용자 ID로 유효한 토큰 삭제 (새로운 로그인 시 기존 토큰 제거)
     */
    @Transactional
    public void deleteValidTokenByUserId(Long userId) {
        accessTokenRepository.deleteValidTokenByUserId(userId, LocalDateTime.now());
    }

    /**
     * 로그아웃 - 토큰 삭제
     */
    @Transactional
    public void logout(String token) {
        accessTokenRepository.deleteByToken(token);
        log.info("로그아웃 완료 - 토큰 삭제됨");
    }
}
