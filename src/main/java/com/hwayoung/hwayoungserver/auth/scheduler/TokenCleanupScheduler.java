package com.hwayoung.hwayoungserver.auth.scheduler;

import com.hwayoung.hwayoungserver.auth.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final AccessTokenService accessTokenService;

    /**
     * 매일 밤 9시에 만료된 토큰 삭제
     * cron = 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 21 * * *")
    public void cleanupExpiredTokens() {
        System.out.println("만료 토큰 정리");
        accessTokenService.deleteExpiredTokens();
    }
}
