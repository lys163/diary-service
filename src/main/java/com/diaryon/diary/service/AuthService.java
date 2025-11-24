package com.diaryon.diary.service;



import com.diaryon.diary.dto.LoginRequest;
import com.diaryon.diary.dto.LoginResponse;
import com.diaryon.diary.dto.SignupRequest;
import com.diaryon.diary.dto.SignupResponse;
import com.diaryon.diary.entity.Role;
import com.diaryon.diary.entity.User;
import com.diaryon.diary.repostitory.UserRepository;
import com.diaryon.diary.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 인증 관련 비즈니스 로직
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request){
        // 1. 중복체크
        if (userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: "+request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: "+request.getEmail());
        }
        // 2. 권한 설정
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        // 3. user 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwd(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .roles(roles)
                .build();

        // 4. 저장
        User savedUser = userRepository.save(user);
        log.info("회원가입 완 userId={}, username={}: ",savedUser.getUserId(),savedUser.getUsername());
        // 응답
        return SignupResponse.builder()
                .userid(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request){
        try{
            // 1. 인증처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.info("인증처리"+authentication);
            // 2. 토큰 생성
            String token = jwtTokenProvider.generateToken(authentication);
            log.info("토큰생성");

            // 3. 사용자 정보 조회
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            log.info("사용자 정보 조회");

            // 4. 권한 정보 추출
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            log.info("권한정보 추출");

            log.info("로그인 성공: username={}",request.getUsername());

            // 5. 응답 생성
            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();
        }catch (BadCredentialsException e){
            log.warn("로그인 실패 : username={}",request.getUsername());
            throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    /**
     * 사용자명 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
