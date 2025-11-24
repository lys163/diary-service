package com.diaryon.diary.service;

import com.diaryon.diary.dto.*;
import com.diaryon.diary.entity.Role;
import com.diaryon.diary.entity.User;
import com.diaryon.diary.repostitory.DiaryRepository;
import com.diaryon.diary.repostitory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 사용자 관리 서비스
 *
 * 주요 기능:
 * 1. 사용자 정보 조회/수정
 * 2. 비밀번호 변경
 * 3. 회원 탈퇴
 * 4. 관리자 기능 (전체 사용자 관리)
 *
 * 보안 주의사항:
 * - 비밀번호는 절대 평문으로 반환하지 않음
 * - 중요한 작업(탈퇴, 비밀번호 변경)은 현재 비밀번호 확인 필수
 * - 다른 사용자의 정보 접근 차단
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== 일반 사용자 기능 ==========

    /**
     * 사용자 정보 조회
     *
     * @param username JWT에서 추출한 사용자명
     * @return 사용자 정보 (비밀번호 제외)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String username) {
        User user = findUserByUsername(username);
        return convertToResponse(user);
    }

    /**
     * 사용자 정보 수정
     *
     * 수정 가능한 필드:
     * - email: 이메일 변경 (중복 체크 필요)
     * - age: 나이 변경
     *
     * 수정 불가능한 필드:
     * - username: Primary Key이므로 변경 불가
     * - password: 별도 API로 변경 (changePassword)
     * - roles: 관리자만 변경 가능
     *
     * @param username 현재 로그인한 사용자명
     * @param request 수정할 정보
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest request) {
        User user = findUserByUsername(username);

        // 이메일 변경 시 중복 체크
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
            }
            // 이메일 변경은 실무에서는 인증 메일 발송 후 확인하는 것이 일반적
            // 학습용이므로 바로 변경
        }

        // 엔티티 수정 (Dirty Checking으로 자동 UPDATE)
        // 실무에서는 User 엔티티에 update 메서드를 만들어 사용하는 것이 좋음
        User updatedUser = User.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(request.getEmail() != null ? request.getEmail() : user.getEmail())
                .passwd(user.getPasswd())
                .age(request.getAge() != null ? request.getAge() : user.getAge())
                .roles(user.getRoles())
                .diaries(user.getDiaries())
                .createdAt(user.getCreatedAt())
                .build();

        userRepository.save(updatedUser);
        log.info("사용자 정보 수정 완료: username={}", username);

        return convertToResponse(updatedUser);
    }

    /**
     * 비밀번호 변경
     *
     * 보안 프로세스:
     * 1. 현재 비밀번호 확인
     * 2. 새 비밀번호 유효성 검증 (DTO에서 수행)
     * 3. 새 비밀번호 암호화
     * 4. DB 업데이트
     *
     * 주의:
     * - 현재 비밀번호가 틀리면 예외 발생
     * - 새 비밀번호는 기존 비밀번호와 달라야 함 (선택사항)
     *
     * @param username 현재 로그인한 사용자명
     * @param request 비밀번호 변경 요청 (현재 비밀번호 + 새 비밀번호)
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        User user = findUserByUsername(username);

        // 1. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswd())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 2. 새 비밀번호가 기존 비밀번호와 같은지 체크 (선택사항)
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswd())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다");
        }

        // 3. 새 비밀번호 암호화 및 저장
        User updatedUser = User.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwd(passwordEncoder.encode(request.getNewPassword()))
                .age(user.getAge())
                .roles(user.getRoles())
                .diaries(user.getDiaries())
                .createdAt(user.getCreatedAt())
                .build();

        userRepository.save(updatedUser);
        log.info("비밀번호 변경 완료: username={}", username);

        // 실무에서는 여기서 다음 작업들을 추가로 수행:
        // - 비밀번호 변경 알림 이메일 발송
        // - 모든 기기에서 로그아웃 (JWT 토큰 무효화)
        // - 비밀번호 변경 이력 저장
    }

    /**
     * 회원 탈퇴
     *
     * 보안 프로세스:
     * 1. 비밀번호 확인 (본인 확인)
     * 2. 연관된 모든 일기 삭제 (cascade로 자동 삭제됨)
     * 3. 사용자 계정 삭제
     *
     * 주의:
     * - 삭제된 데이터는 복구 불가 (실무에서는 soft delete 권장)
     * - JWT 토큰은 자동으로 무효화되지 않음
     * - 클라이언트에서 토큰 삭제 필요
     *
     * @param username 현재 로그인한 사용자명
     * @param password 비밀번호 확인용
     */
    @Transactional
    public void deleteUser(String username, String password) {
        User user = findUserByUsername(username);

        // 비밀번호 확인 (본인 확인)
        if (!passwordEncoder.matches(password, user.getPasswd())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 사용자 삭제 (cascade로 일기도 함께 삭제됨)
        userRepository.delete(user);
        log.info("회원 탈퇴 완료: username={}", username);

        // 실무에서는:
        // 1. Soft Delete (deleted_at 컬럼에 삭제 시간 기록)
        // 2. 탈퇴 사유 수집
        // 3. 일정 기간(30일) 후 완전 삭제
        // 4. 탈퇴 알림 이메일
    }

    // ========== 관리자 전용 기능 ==========

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     *
     * @PreAuthorize("hasRole('ADMIN')")로 권한 체크됨
     *
     * @param pageable 페이징 정보
     * @return 사용자 목록 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToResponse);
    }

    /**
     * 특정 사용자 정보 조회 (관리자 전용)
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return convertToResponse(user);
    }

    /**
     * 사용자 권한 변경 (관리자 전용)
     *
     * 관리자가 특정 사용자에게 권한 부여/제거
     * 예: ROLE_USER → ROLE_ADMIN으로 승격
     *
     * 주의:
     * - 자기 자신의 관리자 권한은 제거할 수 없도록 하는 것이 좋음
     * - 최소 1개 이상의 권한은 있어야 함
     *
     * @param userId 대상 사용자 ID
     * @param request 변경할 권한 목록
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 권한 업데이트
        User updatedUser = User.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .passwd(user.getPasswd())
                .age(user.getAge())
                .roles(request.getRoles())
                .diaries(user.getDiaries())
                .createdAt(user.getCreatedAt())
                .build();

        userRepository.save(updatedUser);
        log.info("사용자 권한 변경 완료: userId={}, newRoles={}", userId, request.getRoles());

        return convertToResponse(updatedUser);
    }

    /**
     * 사용자 계정 강제 삭제 (관리자 전용)
     *
     * 관리자가 특정 사용자의 계정을 강제로 삭제
     * - 해당 사용자의 모든 일기도 함께 삭제
     *
     * @param userId 삭제할 사용자 ID
     */
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        userRepository.delete(user);
        log.info("사용자 삭제 완료 (관리자): userId={}, username={}", userId, user.getUsername());
    }

    /**
     * 사용자 통계 조회 (관리자 전용)
     *
     * 관리자 대시보드용 통계 정보
     * - 전체 사용자 수
     * - 오늘 가입자 수
     * - 이번 달 가입자 수
     * - 권한별 사용자 수
     *
     * @return 통계 정보
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        // 전체 사용자 수
        long totalUsers = userRepository.count();

        // 오늘 가입한 사용자 수
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todaySignups = userRepository.countByCreatedAtBetween(todayStart, todayEnd);

        // 이번 달 가입한 사용자 수
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().atTime(LocalTime.MAX);
        long thisMonthSignups = userRepository.countByCreatedAtBetween(monthStart, monthEnd);

        // 권한별 사용자 수 (JPQL로 계산 - Repository에 메서드 추가 필요)
        long adminCount = userRepository.countByRolesContaining(Role.ROLE_ADMIN);
        long userCount = userRepository.countByRolesContaining(Role.ROLE_USER);

        // 마지막 가입일
        LocalDate lastSignupDate = userRepository.findTopByOrderByCreatedAtDesc()
                .map(user -> user.getCreatedAt().toLocalDate())
                .orElse(null);

        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .todaySignups(todaySignups)
                .thisMonthSignups(thisMonthSignups)
                .adminCount(adminCount)
                .userCount(userCount)
                .lastSignupDate(lastSignupDate)
                .build();
    }

    // ========== Private Helper Methods ==========

    /**
     * 사용자명으로 사용자 조회
     * - 찾지 못하면 예외 발생
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * User 엔티티를 UserResponse DTO로 변환
     *
     * 중요: 비밀번호는 절대 포함하지 않음!
     *
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        // 해당 사용자가 작성한 일기 개수 조회
        long diaryCount = diaryRepository.countByUser(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .age(user.getAge())
                .roles(user.getRoles())
                .diaryCount(diaryCount)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
