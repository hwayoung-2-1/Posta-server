package com.hwayoung.hwayoungserver.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JWT 액세스 토큰을 저장하는 엔티티
 * PostgreSQL UNLOGGED TABLE로 생성되어야 함
 *
 * DDL 예시:
 * CREATE UNLOGGED TABLE access_token (
 *   token VARCHAR(500) PRIMARY KEY,
 *   user_id BIGINT NOT NULL,
 *   expired_at TIMESTAMP NOT NULL,
 *   created_at TIMESTAMP NOT NULL
 * );
 */
@Entity
@Table(name = "access_token")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessToken {

    @Id
    @Column(length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
