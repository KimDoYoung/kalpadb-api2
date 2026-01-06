package kr.co.kalpa.dbapi2.controller;

import jakarta.validation.Valid;
import kr.co.kalpa.dbapi2.dto.ApiResponse;
import kr.co.kalpa.dbapi2.dto.request.LoginRequest;
import kr.co.kalpa.dbapi2.dto.request.TokenRefreshRequest;
import kr.co.kalpa.dbapi2.dto.response.LoginResponse;
import kr.co.kalpa.dbapi2.dto.response.TokenResponse;
import kr.co.kalpa.dbapi2.dto.response.UserInfoResponse;
import kr.co.kalpa.dbapi2.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    /**
     * 로그인 (토큰 발급)
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for userId: {}", request.getUserId());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("로그인 성공", response));
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.info("Token refresh request");

        TokenResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("토큰 갱신 성공", response));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.info("Logout request");

        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success("로그아웃 성공", null));
    }

    /**
     * 토큰 검증
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateToken(
            @RequestHeader("Authorization") String bearerToken) {

        log.info("Token validation request");

        String token = bearerToken.substring(7); // "Bearer " 제거
        boolean isValid = authService.validateToken(token);

        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", isValid);

        return ResponseEntity.ok(
                ApiResponse.success("토큰 검증 완료", result));
    }

    /**
     * 현재 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        log.info("Get current user request");

        UserInfoResponse response = authService.getCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success("사용자 정보 조회 성공", response));
    }
}
