package kr.co.kalpa.api.service;

import kr.co.kalpa.api.dto.request.LoginRequest;
import kr.co.kalpa.api.dto.request.TokenRefreshRequest;
import kr.co.kalpa.api.dto.response.LoginResponse;
import kr.co.kalpa.api.dto.response.TokenResponse;
import kr.co.kalpa.api.dto.response.UserInfoResponse;
import kr.co.kalpa.api.entity.Users;
import kr.co.kalpa.api.exception.InvalidTokenException;
import kr.co.kalpa.api.exception.UnauthorizedException;
import kr.co.kalpa.api.repository.UsersRepository;
import kr.co.kalpa.api.security.RefreshTokenManager;
import kr.co.kalpa.api.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UsersRepository usersRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for userId: {}", request.getUserId());

        // 사용자 존재 확인
        Users user = usersRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 비밀번호 검증 (BCrypt)
        if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
            log.warn("Invalid password for user: {}", request.getUserId());
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다");
        }

        // 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserId(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Refresh Token 저장
        refreshTokenManager.saveRefreshToken(refreshToken, user.getUserId());

        log.info("User logged in successfully: {}", user.getUserId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserNm())
                        .build())
                .build();
    }

    /**
     * 토큰 갱신
     */
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        log.debug("Token refresh attempt");

        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token입니다");
        }

        // Refresh Token으로 사용자 ID 조회
        String userId = refreshTokenManager.getUserIdByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh Token을 찾을 수 없습니다"));

        // 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        log.info("Token refreshed for user: {}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .build();
    }

    /**
     * 로그아웃
     */
    public void logout(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        log.debug("Logout attempt");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh Token이 필요합니다");
        }

        // Refresh Token 삭제 및 블랙리스트 추가
        refreshTokenManager.removeRefreshToken(refreshToken);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully");
    }

    /**
     * 현재 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다");
        }

        String userId = authentication.getName();

        Users user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + userId));

        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserNm())
                .build();
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
