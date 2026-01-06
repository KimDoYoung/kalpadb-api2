package kr.co.kalpa.dbapi2.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RefreshTokenManager {
    /**
     * Refresh Token 저장소 (메모리 기반)
     * Key: Refresh Token, Value: 사용자 ID
     */
    private final ConcurrentHashMap<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    /**
     * 블랙리스트 (로그아웃된 토큰)
     * Key: Refresh Token, Value: 만료 시간
     */
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    /**
     * Refresh Token 저장
     */
    public void saveRefreshToken(String refreshToken, String userId) {
        refreshTokenStore.put(refreshToken, userId);
        log.debug("Saved refresh token for user: {}", userId);
    }

    /**
     * Refresh Token으로 사용자 ID 조회
     */
    public Optional<String> getUserIdByRefreshToken(String refreshToken) {
        // 블랙리스트 확인
        if (isBlacklisted(refreshToken)) {
            log.warn("Attempted to use blacklisted refresh token");
            return Optional.empty();
        }

        return Optional.ofNullable(refreshTokenStore.get(refreshToken));
    }

    /**
     * Refresh Token 삭제 (로그아웃)
     */
    public void removeRefreshToken(String refreshToken) {
        String userId = refreshTokenStore.remove(refreshToken);

        if (userId != null) {
            // 블랙리스트에 추가 (7일 후 자동 삭제)
            long expiryTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
            tokenBlacklist.put(refreshToken, expiryTime);
            log.debug("Removed refresh token for user: {}", userId);
        }
    }

    /**
     * 블랙리스트 확인
     */
    public boolean isBlacklisted(String refreshToken) {
        Long expiryTime = tokenBlacklist.get(refreshToken);

        if (expiryTime == null) {
            return false;
        }

        // 만료된 블랙리스트 항목 제거
        if (System.currentTimeMillis() > expiryTime) {
            tokenBlacklist.remove(refreshToken);
            return false;
        }

        return true;
    }

    /**
     * 만료된 블랙리스트 항목 정리 (스케줄러로 주기적 실행)
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void cleanupExpiredBlacklist() {
        long now = System.currentTimeMillis();
        int removed = 0;

        for (var entry : tokenBlacklist.entrySet()) {
            if (entry.getValue() < now) {
                tokenBlacklist.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            log.info("Cleaned up {} expired blacklist entries", removed);
        }
    }
}
