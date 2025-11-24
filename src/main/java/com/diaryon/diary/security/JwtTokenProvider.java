package com.diaryon.diary.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 담당 클래스
 * - 로그인 성공 시 JWT 토큰 생성
 * - API 요청 시 JWT 토큰 검증
 * - 토큰에서 사용자 정보 추출
 */

@Component
@Slf4j
public class JwtTokenProvider {
    // application.properties에서 주입
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 만료 시간 (밀리초)
    // application.properties에서 주입
    // jwt.expiration=86400000 (24시간)
    @Value("${jwt.expiration}")
    private Long validityInMilliseconds;

    /**
     * JWT 토큰 생성
     * - 로그인 성공 시 호출
     * - username, roles 정보를 토큰에 포함
     *
     * @param authentication Spring Security 인증 객체
     * @return JWT 토큰 문자열
     */

    public String generateToken(Authentication authentication){

        // 인증된 사용자 정보 추출
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 권한 목록을 쉼표로 구분된 문자열로 변환
        // 예: "ROLE_USER,ROLE_ADMIN"
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        // 만료시간
        Date expiryDate = new Date(now.getTime()+validityInMilliseconds);

        //JWT토큰 생성
        return Jwts.builder()
                .setSubject(userDetails.getUsername())                   // 사용자명 (payload의 sub)
                .claim("roles",roles)                              // 권한 정보 (payload의 roles)
                .setIssuedAt(now)                                        // 발급 시간 (iat)
                .setExpiration(expiryDate)                               // 만료 시간 (exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)     // 서명 (HMAC SHA-256)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자명 추출
     *
     * @param token JWT 토큰
     * @return 사용자명 (username)
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 권한 정보 추출
     *
     * @param token JWT 토큰
     * @return 권한 문자열 (예: "ROLE_USER,ROLE_ADMIN")
     */
    public String getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", String.class);
    }

    /**
     * JWT 토큰 유효성 검증
     * - 서명 검증
     * - 만료 시간 확인
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 유효하지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명
            System.err.println("잘못된 JWT 서명입니다: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            // 만료된 JWT 토큰
            System.err.println("만료된 JWT 토큰입니다: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 토큰
            System.err.println("지원되지 않는 JWT 토큰입니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // JWT 토큰이 잘못되었습니다
            System.err.println("JWT 토큰이 잘못되었습니다: " + e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰 파싱 (Claims 추출)
     * - 서명 검증 포함
     *
     * @param token JWT 토큰
     * @return Claims (토큰의 payload 정보)
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 서명용 SecretKey 생성
     * - HMAC SHA-256 알고리즘 사용
     * - 최소 256비트(32바이트) 이상의 키 필요
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
