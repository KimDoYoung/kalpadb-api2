package kr.co.kalpa.ui.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class AuthController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        log.info("▶▶▶ AuthController.login() 호출됨");
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("⚠️ 이미 로그인된 사용자입니다: {}", authentication.getName());
            return "redirect:/";
        }
        log.info("로그인 페이지 뷰(auth/login) 반환");
        return "auth/login";
    }

}
