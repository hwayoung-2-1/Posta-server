package com.hwayoung.hwayoungserver.auth.repository;

import com.hwayoung.hwayoungserver.auth.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {

    /**
     * 토큰으로 만료되지 않은 토큰 조회
     */
    @Query("SELECT a FROM AccessToken a WHERE a.token = :token AND a.expiredAt > :now")
    Optional<AccessToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.expiredAt <= :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 로그인 중인지 확인
     */
    @Query("SELECT a FROM AccessToken a WHERE a.userId = :user_id AND a.expiredAt > :now")
    Optional<AccessToken> findLoggedinToken(@Param("user_id") Long user_id, @Param("now") LocalDateTime now);

    /**
     * 사용자 ID로 유효한 토큰 삭제 (새로운 로그인 시 기존 토큰 제거)
     */
    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.userId = :userId AND a.expiredAt > :now")
    void deleteValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 토큰으로 삭제 (로그아웃)
     */
    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.token = :token")
    void deleteByToken(@Param("token") String token);

}
