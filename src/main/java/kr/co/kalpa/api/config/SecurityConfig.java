package kr.co.kalpa.api.config;

import kr.co.kalpa.api.security.CustomUserDetailsService;
import kr.co.kalpa.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // API는 CSRF 비활성화
                        .disable()) // UI에서도 일단 비활성화 (나중에 필요시 활성화)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Session 설정 (UI: SESSION, API: STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스는 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 홈페이지와 로그인 페이지는 허용
                        .requestMatchers("/", "/login").permitAll()

                        // Swagger UI 및 API Docs 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // REST API 경로
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/todo/**").permitAll()
                        .requestMatchers("/api/**").authenticated()

                        // UI 페이지는 로그인 필요
                        .requestMatchers("/diary/**", "/todo/**", "/books/**", "/movies/**", "/essay/**", "/notes/**",
                                "/calendar/**")
                        .authenticated()

                        // 그 외 경로
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                // JWT 필터 추가 (API 인증용)
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // 폼 로그인 설정 (UI 인증용)
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/", true)
                        .successHandler((request, response, authentication) -> {
                            log.info("✅ 로그인 성공: {} (권한: {})",
                                    authentication.getName(),
                                    authentication.getAuthorities());
                            response.sendRedirect(request.getContextPath() + "/");
                        })
                        .failureHandler((request, response, exception) -> {
                            log.warn("❌ 로그인 실패: {} - {}",
                                    request.getParameter("username"),
                                    exception.getMessage());
                            response.sendRedirect(request.getContextPath() + "/login?error");
                        }))

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            log.info("🔐 로그아웃 성공: {}", authentication != null ? authentication.getName() : "anonymous");
                            response.sendRedirect(request.getContextPath() + "/login");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // API 요청이면 JSON 응답 (getServletPath()는 context-path 제외)
                            if (request.getServletPath().startsWith("/api/")) {
                                log.error("Unauthorized error: {}", authException.getMessage());
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                String jsonResponse = String.format(
                                        "{\"success\":false,\"message\":\"인증이 필요합니다\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
                                        authException.getMessage(),
                                        LocalDateTime.now().toString());
                                response.getWriter().write(jsonResponse);
                            } else {
                                // UI 요청이면 로그인 페이지로 리다이렉트
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.error("Access denied error: {}", accessDeniedException.getMessage());
                            // API 요청이면 JSON 응답 (getServletPath()는 context-path 제외)
                            if (request.getServletPath().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");
                                String jsonResponse = String.format(
                                        "{\"success\":false,\"message\":\"접근 권한이 없습니다\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
                                        accessDeniedException.getMessage(),
                                        LocalDateTime.now().toString());
                                response.getWriter().write(jsonResponse);
                            } else {
                                // UI 요청이면 403 에러 페이지
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.sendRedirect(request.getContextPath() + "/error");
                            }
                        }));

        return http.build();
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발 환경: 모든 Origin 허용
        configuration.addAllowedOriginPattern("*");

        // 허용할 HTTP 메서드
        configuration.addAllowedMethod("*");

        // 허용할 헤더
        configuration.addAllowedHeader("*");

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password Encoder (BCryptPasswordEncoder 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager 설정
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
