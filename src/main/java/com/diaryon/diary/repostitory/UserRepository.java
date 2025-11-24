package com.diaryon.diary.repostitory;

import com.diaryon.diary.entity.Role;
import com.diaryon.diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User 엔티티 Repository
 *
 * Spring Data JPA가 제공하는 기능:
 * 1. 기본 CRUD: save(), findById(), findAll(), delete() 등
 * 2. 쿼리 메서드: 메서드 이름으로 쿼리 자동 생성
 * 3. 페이징/정렬: Pageable, Sort 지원
 *
 * 쿼리 메서드 네이밍 규칙:
 * - findBy + 필드명: 조회
 * - existsBy + 필드명: 존재 여부
 * - countBy + 필드명: 개수
 * - deleteBy + 필드명: 삭제
 *
 * 예시:
 * - findByUsername → SELECT * FROM users WHERE username = ?
 * - existsByEmail → SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
 * - countByCreatedAtBetween → SELECT COUNT(*) FROM users WHERE created_at BETWEEN ? AND ?
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 기본 조회 ==========

    /**
     * 사용자명으로 사용자 조회
     * - 로그인 시 사용
     * - CustomUserDetailsService에서 사용
     *
     * 생성되는 쿼리:
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자 조회
     * - 이메일 로그인 지원 시 사용
     *
     * 생성되는 쿼리:
     * SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    // ========== 중복 체크 ==========

    /**
     * 사용자명 중복 체크
     * - 회원가입 시 사용
     *
     * 생성되는 쿼리:
     * SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)
     *
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 중복 체크
     * - 회원가입 및 이메일 변경 시 사용
     *
     * 생성되는 쿼리:
     * SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);

    // ========== 통계용 쿼리 (관리자 기능) ==========

    /**
     * 특정 기간 내 가입한 사용자 수 조회
     * - 오늘 가입자, 이번 달 가입자 조회에 사용
     *
     * 생성되는 쿼리:
     * SELECT COUNT(*) FROM users WHERE created_at BETWEEN ? AND ?
     *
     * 예시:
     * - 오늘 가입자: countByCreatedAtBetween(오늘 00:00, 오늘 23:59)
     * - 이번 달: countByCreatedAtBetween(이번달 1일, 오늘)
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 권한을 가진 사용자 수 조회
     * - 관리자 수, 일반 사용자 수 조회
     *
     * 생성되는 쿼리:
     * SELECT COUNT(*) FROM users u
     * JOIN user_roles ur ON u.user_id = ur.user_id
     * WHERE ur.role = ?
     *
     * 주의: @ElementCollection으로 매핑된 roles를 조회
     */
    long countByRolesContaining(Role role);

    /**
     * 가장 최근에 가입한 사용자 조회
     * - 마지막 가입일 조회에 사용
     *
     * 생성되는 쿼리:
     * SELECT * FROM users ORDER BY created_at DESC LIMIT 1
     */
    Optional<User> findTopByOrderByCreatedAtDesc();

    // ========== 추가 기능 (선택사항) ==========

    /**
     * 특정 나이 범위의 사용자 수
     * - 사용자 연령대 통계
     *
     * 예시: countByAgeBetween(20, 29) → 20대 사용자 수
     */
    long countByAgeBetween(Integer minAge, Integer maxAge);

    /**
     * 특정 날짜 이후 가입한 사용자 목록
     * - 최근 가입자 조회
     *
     * 예시: findByCreatedAtAfter(7일 전) → 최근 7일 가입자
     */
    java.util.List<User> findByCreatedAtAfter(LocalDateTime date);


}
