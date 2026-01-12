package kr.co.kalpa.api.config;

import kr.co.kalpa.api.entity.Users;
import kr.co.kalpa.api.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 기본 사용자 데이터 초기화 (새 사용자만 생성)
        String userId = "kdy987";
        String password = "1111";
        String encodedPassword = passwordEncoder.encode(password);

        var existingUser = usersRepository.findByUserId(userId);

        if (existingUser.isEmpty()) {
            Users user = Users.builder()
                    .userId(userId)
                    .userPw(encodedPassword)
                    .userNm("KimDoYoung")
                    .build();
            usersRepository.save(user);
            log.info("✅ 기본 사용자 생성: {}", userId);
        } else {
            log.info("ℹ️ 사용자 {} 는 이미 존재합니다", userId);
        }
    }
}
