package kr.co.kalpa.api.security;

import kr.co.kalpa.api.entity.Users;
import kr.co.kalpa.api.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.info("🔍 사용자 조회 시작: {}", userId);

        Users user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("❌ 사용자를 찾을 수 없습니다: {}", userId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        log.info("✅ 사용자 로드 성공: {} (이름: {})", userId, user.getUserNm());
        return new CustomUserDetails(user);
    }
}
