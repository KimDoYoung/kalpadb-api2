package kr.co.kalpa.api;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBcryptHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "1111";
        String hash = encoder.encode(password);
        System.out.println("비밀번호: " + password);
        System.out.println("bcrypt 해시값: " + hash);

        // 검증 테스트
        boolean matches = encoder.matches(password, hash);
        System.out.println("일치 여부: " + matches);
    }
}
