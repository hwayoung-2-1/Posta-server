package com.hwayoung.hwayoungserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * access_token 테이블을 UNLOGGED TABLE로 자동 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE access_token SET UNLOGGED");
            log.info("access_token 테이블을 UNLOGGED로 변환 완료");
        } catch (Exception e) {
            log.warn("뭔가 이상한데 재실행 권장(access_token테이블 변환중 이상 발생) : {}", e.getMessage());
        }
    }
}
