package kr.co.kalpa.ui.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /*
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("로그아웃 시작: {}", authentication != null ? authentication.getName() : "anonymousUser");

        // Spring Security 로그아웃 처리
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        log.info("로그아웃 완료 - 로그인 페이지로 리다이렉트");
        return "redirect:/login";
    }
    */
}
