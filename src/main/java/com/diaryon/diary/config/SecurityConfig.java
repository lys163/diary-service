package com.diaryon.diary.config;

import com.diaryon.diary.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.util.Arrays;

/**
 * Spring Security 설정
 * - JWT 기반 인증 구현
 * - 비밀번호 암호화 (BCrypt)
 * - CORS 설정
 * - URL별 접근 권한 설정
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * 비밀번호 암호화 Bean
     * - BCrypt 알고리즘 사용 (단방향 해시)
     * - 회원가입 시 비밀번호 암호화, 로그인 시 비밀번호 검증에 사용
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    /**
     * AuthenticationManager Bean
     * - 로그인 시 사용자 인증 처리
     *
     * @param authenticationConfiguration Spring이 제공하는 설정
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // CSRF 비활성화 (JWT 사용 시 불필요, REST API이므로)
                .csrf(csrf->csrf.disable())
                // CORS 설정 활성화
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                // 세션 사용 안 함 (JWT 기반 Stateless 인증)
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth->auth
                        // 인증 없이 접근 가능한 URL (회원가입, 로그인)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        // 관리자 전용 URL
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();



    }
    /**
     * CORS 설정
     * - React 프론트엔드와의 통신을 위한 설정
     * - 프로덕션에서는 allowedOrigins를 실제 도메인으로 변경 필요
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 Origin (React 개발 서버)
        // 프로덕션: 실제 도메인으로 변경 (예: "https://mydiary.com")
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 허용할 메서드
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);

        return source;


    }
}
