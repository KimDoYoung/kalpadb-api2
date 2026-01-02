package kr.co.kalpa.dbapi2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KalpaDbApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KalpaDbApiApplication.class, args);
    }

}
