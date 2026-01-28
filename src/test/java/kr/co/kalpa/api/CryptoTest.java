package kr.co.kalpa.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoTest {

    @Test
    void generateHash() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1111";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("=== PASSWORD HASH GENERATION ===");
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println("================================");

        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
    }
}
