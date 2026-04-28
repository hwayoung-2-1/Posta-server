package com.hwayoung.hwayoungserver.auth.repository;

import com.hwayoung.hwayoungserver.auth.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {

    /**
     * 토큰으로 만료되지 않은 토큰 조회
     */
    Optional<AccessToken> findByTokenAndExpiredAtAfter(String token, LocalDateTime now);

    /**
     * 만료된 토큰 삭제
     */
    int deleteByExpiredAtBefore(LocalDateTime now);

    /**
     * 로그인 중인지 확인
     */
    Optional<AccessToken> findByUserIdAndExpiredAtAfter(UUID userId, LocalDateTime now);

    /**
     * 사용자 ID로 유효한 토큰 삭제 (새로운 로그인 시 기존 토큰 제거)
     */
    void deleteByUserIdAndExpiredAtAfter(UUID userId, LocalDateTime now);

    /**
     * 토큰으로 삭제 (로그아웃)
     */
    void deleteByToken(String token);

}
