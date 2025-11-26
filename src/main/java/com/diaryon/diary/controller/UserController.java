package com.diaryon.diary.controller;

import com.diaryon.diary.dto.*;
import com.diaryon.diary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 관리 API 컨트롤러
 *
 * 권한 구분:
 * 1. 일반 사용자(ROLE_USER): 자신의 정보만 조회/수정 가능
 * 2. 관리자(ROLE_ADMIN): 모든 사용자 관리 가능
 *
 * Spring Security 주요 애노테이션:
 * - @AuthenticationPrincipal: JWT에서 추출한 username을 자동으로 주입
 * - @PreAuthorize: 메서드 실행 전 권한 체크
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/users/me
     *
     * 인증된 사용자라면 누구나 자신의 정보 조회 가능
     * @AuthenticationPrincipal: JwtAuthenticationFilter에서 SecurityContext에 저장한
     * Authentication 객체의 principal(username)을 자동으로 주입
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal String username) {
        log.info("내 정보 조회: username={}", username);
        UserResponse response = userService.getUserInfo(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 정보 수정
     * PUT /api/users/me
     *
     * 자신의 정보만 수정 가능:
     * - 이메일 변경
     * - 나이 변경
     * - 비밀번호 변경 (별도 API 권장)
     *
     * 주의: username은 변경 불가 (Primary Key로 사용)
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("내 정보 수정: username={}", username);
        UserResponse response = userService.updateUser(username, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     * PUT /api/users/me/password
     *
     * 보안을 위해 기존 비밀번호 확인 필수
     * - 기존 비밀번호가 일치해야만 변경 가능
     * - 새 비밀번호는 유효성 검증 통과해야 함
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody PasswordChangeRequest request, BindingResult result) {

        if (result.hasErrors()){
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage) // 각 에러 메시지 추출
                    .collect(Collectors.toList());
            return ResponseEntity.status(400).body(errorMessages);
        }
        try{
            userService.changePassword(username, request);
            return ResponseEntity.ok("비밀번호가 변경되었습니다.");
        }catch (Exception e){
            return ResponseEntity.status(400).body(e);
        }



    }

    /**
     * 회원 탈퇴
     * DELETE /api/users/me
     *
     * 사용자가 자신의 계정 삭제
     * - 모든 일기도 함께 삭제 (cascade)
     * - 비밀번호 확인 필수 (보안)
     * - 삭제 후 JWT 토큰은 무효화되지 않으므로 클라이언트에서 토큰 삭제 필요
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal String username
            //, @Valid @RequestBody PasswordConfirmRequest request
    ) {
        log.info("회원 탈퇴 요청: username={}", username);
        userService.deleteUser(username
                //, request.getPassword()
        );
        return ResponseEntity.noContent().build();
    }

    // ========== 관리자 전용 API ==========

    /**
     * 모든 사용자 목록 조회 (관리자 전용)
     * GET /api/users?page=0&size=10&sort=createdAt,desc
     *
     * @PreAuthorize("hasRole('ADMIN')"):
     * - ROLE_ADMIN 권한이 있는 사용자만 접근 가능
     * - 권한이 없으면 403 Forbidden 응답
     *
     * Spring Security 내부 동작:
     * 1. JwtAuthenticationFilter에서 JWT 토큰 검증
     * 2. SecurityContext에 Authentication 저장 (권한 포함)
     * 3. @PreAuthorize가 권한 체크
     * 4. 통과하면 메서드 실행, 실패하면 AccessDeniedException
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("전체 사용자 목록 조회 (관리자)");
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 정보 조회 (관리자 전용)
     * GET /api/users/{userId}
     *
     * 관리자는 모든 사용자의 정보를 조회할 수 있음
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("사용자 정보 조회 (관리자): userId={}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 권한 변경 (관리자 전용)
     * PUT /api/users/{userId}/roles
     *
     * 관리자가 특정 사용자에게 권한 부여/제거
     * 예: 일반 사용자를 관리자로 승격
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        log.info("사용자 권한 변경 (관리자): userId={}, roles={}", userId, request.getRoles());
        UserResponse response = userService.updateUserRoles(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 계정 삭제 (관리자 전용)
     * DELETE /api/users/{userId}
     *
     * 관리자가 특정 사용자 계정 강제 삭제
     * - 해당 사용자의 모든 일기도 함께 삭제
     * - 실무에서는 soft delete(논리 삭제) 권장
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserByAdmin(@PathVariable Long userId) {
        log.info("사용자 삭제 (관리자): userId={}", userId);
        userService.deleteUserByAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자 통계 조회 (관리자 전용)
     * GET /api/users/stats
     *
     * 전체 사용자 수, 오늘 가입자 수 등의 통계
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        log.info("사용자 통계 조회 (관리자)");
        UserStatsResponse stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }
}
