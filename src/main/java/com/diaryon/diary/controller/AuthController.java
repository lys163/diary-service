package com.diaryon.diary.controller;

import com.diaryon.diary.dto.LoginRequest;
import com.diaryon.diary.dto.LoginResponse;
import com.diaryon.diary.dto.SignupRequest;
import com.diaryon.diary.dto.SignupResponse;
import com.diaryon.diary.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    /**
     * 회원가입 API
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request){
        log.info("회원가입 요청");
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        log.info("로그인 요청");
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자명 중복 체크 API
     * GET /api/auth/check-username?username=test
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = authService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /**
     * 이메일 중복 체크 API
     * GET /api/auth/check-email?email=test@test.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = authService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
