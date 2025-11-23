package com.diaryon.diary.security;

import com.diaryon.diary.entity.User;
import com.diaryon.diary.repostitory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {



    private final UserRepository userRepository;

    /**
     * 사용자명(username)으로 사용자 정보 조회
     * - Spring Security가 로그인 시 자동으로 호출
     * - @ElementCollection(EAGER)로 roles도 함께 조회됨 (N+1 문제 없음)
     *
     * @param username 사용자명
     * @return UserDetails (Spring Security 인증 객체)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */

    @Override
    @Transactional(readOnly = true) //읽기전용 트랜젝션
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. db에서 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("CustomUserDetailsService - 사용자 찾을 수 없음 : "+username));

        // 2. Entity의 Role Enum을 Spring Security의 GrantedAuthority로 변환
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        // 3. Spring Security의 User 객체로 변환하여 반환
        // (Entity User와 다름, org.springframework.security.core.userdetails.User)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswd())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
    /**
     * 이메일로 사용자 정보 조회
     * - 이메일 로그인을 지원할 경우 사용
     * - AuthenticationProvider에서 별도로 호출
     *
     * @param email 이메일
     * @return UserDetails
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + email
                ));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswd())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
