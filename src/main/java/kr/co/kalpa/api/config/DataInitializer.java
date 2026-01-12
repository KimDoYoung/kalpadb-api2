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
        // 기본 사용자 데이터 초기화
        String userId = "kdy987";
        String password = "1111";
        String encodedPassword = passwordEncoder.encode(password);

        log.info("========== 기본 사용자 초기화 시작 ==========");
        log.info("사용자ID: {}", userId);
        log.info("비밀번호: {} (원본)", password);

        var existingUser = usersRepository.findByUserId(userId);

        if (existingUser.isEmpty()) {
            // 사용자 생성
            Users user = Users.builder()
                    .userId(userId)
                    .userPw(encodedPassword)
                    .userNm("KimDoYoung")
                    .build();
            usersRepository.save(user);
            log.info("✅ 기본 사용자 생성 완료: {}", userId);
        } else {
            // 기존 사용자의 비밀번호 업데이트
            Users user = existingUser.get();
            user.setUserPw(encodedPassword);
            usersRepository.save(user);
            log.info("✅ 사용자 {} 비밀번호 업데이트 완료", userId);
        }
        log.info("🔐 암호화된 비밀번호: {}", encodedPassword);
        log.info("========== 기본 사용자 초기화 완료 ==========");
    }
}
