package kr.co.kalpa.dbapi2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // @Scheduled 어노테이션 활성화
    // RefreshTokenManager의 cleanupExpiredBlacklist() 메서드를 위해 필요
}
