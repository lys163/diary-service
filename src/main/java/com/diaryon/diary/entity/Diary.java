package com.diaryon.diary.entity;

import com.diaryon.diary.entity.Mood;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일기 엔티티
 * - 사용자별 일기 정보 저장
 * - User와 N:1 관계
 * - 하루에 하나의 일기만 작성 가능 (UNIQUE 제약조건)
 */
@Entity // JPA 엔티티임을 명시
@Table(name = "diaries",
        // 같은 사용자가 같은 날짜에 일기를 중복 작성할 수 없도록 제약조건 설정
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "diary_date"}),
        // user_id와 diary_date로 자주 조회하므로 복합 인덱스 생성 (조회 성능 향상)
        indexes = @Index(name = "idx_user_date", columnList = "user_id, diary_date"))
@Getter // 모든 필드의 getter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 (JPA가 리플렉션으로 객체 생성 시 필요)
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 (Builder와 함께 사용)
@Builder // 빌더 패턴으로 객체 생성 (가독성, 불변성)
public class Diary {

    @Id // 기본키(Primary Key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동 생성 (DB의 AUTO_INCREMENT 사용)
    private Long id;

    /**
     * 일기 작성자 (User와의 N:1 관계)
     * - LAZY 로딩: 일기 조회 시 User 정보가 항상 필요한 것은 아니므로 지연 로딩
     * - optional = false: User는 필수 (일기는 반드시 작성자가 있어야 함)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // 외래키 컬럼명 지정
    private User user;

    @Column(nullable = false, length = 200) // 제목은 필수, 최대 200자
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // 내용은 필수, 긴 텍스트 저장
    private String content;

    @Column(name = "diary_date", nullable = false) // 일기 작성 날짜 (필수)
    private LocalDate diaryDate;

    /**
     * 일기 작성 시의 기분 상태
     * - STRING으로 저장: "HAPPY", "SAD" 등 문자열 저장 (안전함)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Mood mood;

    @Column(name = "created_at", nullable = false, updatable = false) // 생성일은 수정 불가
    @CreationTimestamp // INSERT 시 자동으로 현재 시간 설정
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp // UPDATE 시 자동으로 현재 시간 설정
    private LocalDateTime updatedAt;

    /**
     * 엔티티 저장/수정 전 자동 실행되는 메서드
     * - 데이터 정규화: 공백 제거
     * - 기본값 설정: 날짜가 없으면 오늘 날짜로 설정
     */
    @PrePersist // INSERT 전 실행
    @PreUpdate // UPDATE 전 실행
    private void prepare() {
        // 제목 앞뒤 공백 제거
        if (title != null) {
            title = title.trim();
        }
        // 내용 앞뒤 공백 제거
        if (content != null) {
            content = content.trim();
        }
        // 날짜가 입력되지 않았으면 오늘 날짜로 설정
        if (diaryDate == null) {
            diaryDate = LocalDate.now();
        }
    }
}