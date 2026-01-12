package kr.co.kalpa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "kr.co.kalpa")
@EnableJpaAuditing
public class KalpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KalpaApplication.class, args);
    }

}
