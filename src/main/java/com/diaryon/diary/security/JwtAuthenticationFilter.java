package com.diaryon.diary.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청마다 실행
 * - Authorization 헤더에서 JWT 토큰 추출
 * - 토큰 검증 후 SecurityContext에 인증 정보 저장
 * - OncePerRequestFilter 상속으로 요청당 한 번만 실행 보장
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 필터 로직 구현
     * - 요청에서 JWT 토큰 추출
     * - 토큰 검증
     * - 유효한 토큰이면 SecurityContext에 인증 정보 설정
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            //1. 요청 헤더에서 JWT토큰 추출
            String token = extractTokenFromRequest(request);
            // 2. 토큰이 있고 유효한지 검증
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)){

                // 3. 토큰에서 사용자 정보 추출
                String username = jwtTokenProvider.getUsernameFromToken(token);
                String rolesString = jwtTokenProvider.getRolesFromToken(token);

                // 4. 권한 문자열을 GrantedAuthority 리스트로 변환
                // "ROLE_USER,ROLE_ADMIN" -> [ROLE_USER, ROLE_ADMIN]
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 5. Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,       // principal (사용자명)
                                null,           // credentials (비밀번호는 불필요, 이미 인증됨)
                                authorities     // authorities (권한 목록)
                        );
                // 6. 요청 세부 정보 설정(IP주소, 세션 id 등)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 7. SecurityContext에 인증 정보 저장
                // 이후 컨트롤러에서 @AuthenticationPrincipal, SecurityContextHolder 등으로 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception e){
            log.error("JwtAuthenticationFilter doFilterInternal() 중 에러"+e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     * - Authorization 헤더 형식: "Bearer {token}"
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String extractTokenFromRequest(HttpServletRequest request){
        // Authorization 헤더 값 가져오기
        String bearerToken = request.getHeader("Authorization");
        // "Bearer " 로 시작하면 토큰 부분만 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); // "Bearer " 부분 삭제 토큰만가져오기
        }
        return null;
    }
}
