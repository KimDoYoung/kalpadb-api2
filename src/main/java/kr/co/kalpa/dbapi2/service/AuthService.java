package kr.co.kalpa.dbapi2.service;

import kr.co.kalpa.dbapi2.dto.request.LoginRequest;
import kr.co.kalpa.dbapi2.dto.request.TokenRefreshRequest;
import kr.co.kalpa.dbapi2.dto.response.LoginResponse;
import kr.co.kalpa.dbapi2.dto.response.TokenResponse;
import kr.co.kalpa.dbapi2.dto.response.UserInfoResponse;
import kr.co.kalpa.dbapi2.entity.ApUser;
import kr.co.kalpa.dbapi2.exception.InvalidTokenException;
import kr.co.kalpa.dbapi2.exception.UnauthorizedException;
import kr.co.kalpa.dbapi2.repository.ApUserRepository;
import kr.co.kalpa.dbapi2.security.RefreshTokenManager;
import kr.co.kalpa.dbapi2.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final ApUserRepository apUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;
    private final AuthenticationManager authenticationManager;

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for userId: {}", request.getUserId());

        // 사용자 존재 확인
        ApUser apUser = apUserRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 비밀번호 검증 (현재는 평문 비교)
        if (!apUser.getUserPw().equals(request.getPassword())) {
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
        String accessToken = jwtTokenProvider.generateAccessToken(apUser.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(apUser.getUserId());

        // Refresh Token 저장
        refreshTokenManager.saveRefreshToken(refreshToken, apUser.getUserId());

        log.info("User logged in successfully: {}", apUser.getUserId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(apUser.getUserId())
                        .userName(apUser.getUserNm())
                        .userHomeFolder(apUser.getUserHomeFolder())
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

        ApUser apUser = apUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + userId));

        return UserInfoResponse.builder()
                .userId(apUser.getUserId())
                .userName(apUser.getUserNm())
                .userHomeFolder(apUser.getUserHomeFolder())
                .build();
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
