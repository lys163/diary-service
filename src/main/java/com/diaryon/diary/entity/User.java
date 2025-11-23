package com.diaryon.diary.entity;

import com.diaryon.diary.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 사용자 엔티티
 * - 회원가입/로그인 정보를 저장
 * - 권한(Role) 관리
 * - 일기(Diary)와 1:N 관계
 */
@Entity // JPA 엔티티임을 명시
@Table(name = "users") // DB 테이블명 지정
@Getter // 모든 필드의 getter 메서드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 protected로 생성 (무분별한 객체 생성 방지, JPA는 접근 가능)
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 (Builder와 함께 사용)
@Builder // 빌더 패턴으로 객체 생성 (가독성, 불변성)
public class User {

    @Id // 기본키(Primary Key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동 생성 (DB의 AUTO_INCREMENT 사용)
    @Column(name = "user_id") // 컬럼명 명시적 지정
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50) // NOT NULL, UNIQUE 제약조건, 최대 길이 50
    private String username; // 로그인용 사용자명

    @Column(name = "email", nullable = false, unique = true, length = 100) // 이메일도 중복 불가
    private String email;

    @Column(name = "passwd", nullable = false) // 암호화된 비밀번호 저장 (BCrypt 사용 예정)
    private String passwd;

    @Column(name = "age") // 나이 (선택사항, nullable = true가 기본값)
    private Integer age; // int 대신 Integer 사용 (null 가능하도록)

    /**
     * 사용자 권한 관리
     * - Set 사용 이유: 중복 권한 방지
     * - EAGER 로딩: Spring Security 인증 시 항상 권한이 필요하므로 즉시 로딩
     */
    @ElementCollection(fetch = FetchType.EAGER) // 컬렉션을 별도 테이블에 저장, 즉시 로딩
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // 권한 저장 테이블 지정
    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장 (ORDINAL은 순서 변경 시 문제 발생)
    @Column(name = "role") // 컬럼명 지정
    @Builder.Default // Builder 사용 시 기본값 적용
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false) // 생성일은 수정 불가
    @CreationTimestamp // INSERT 시 자동으로 현재 시간 설정
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp // UPDATE 시 자동으로 현재 시간 설정
    private LocalDateTime updatedAt;

    /**
     * User와 Diary의 1:N 관계 설정
     * - mappedBy: 연관관계의 주인은 Diary.user
     * - cascade: User 삭제 시 관련 Diary도 모두 삭제
     * - orphanRemoval: 관계가 끊긴 Diary 자동 삭제
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Diary> diaries = new ArrayList<>();

    /**
     * 엔티티 저장/수정 전 자동 실행되는 메서드
     * - 데이터 정규화: 공백 제거, 소문자 변환
     * - 기본값 설정: 권한이 없으면 ROLE_USER 부여
     * - 유효성 검증: 나이는 0 이상이어야 함
     */
    @PrePersist // INSERT 전 실행
    @PreUpdate // UPDATE 전 실행
    private void prepare() {
        // username 앞뒤 공백 제거
        if (username != null) {
            username = username.trim();
        }
        // email 소문자 변환 및 공백 제거 (이메일은 대소문자 구분 안 함)
        if (email != null) {
            email = email.toLowerCase().trim();
        }
        // 권한이 없으면 기본 권한(ROLE_USER) 부여
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
        }
        // 나이 유효성 검증 (음수 방지)
        if (age != null && age < 0) {
            throw new IllegalArgumentException("나이는 0 이상이어야 합니다.");
        }
    }
}