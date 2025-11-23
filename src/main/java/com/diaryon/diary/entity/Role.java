package com.diaryon.diary.entity;
/**
 * 사용자 권한 Enum
 * - Spring Security에서 사용
 * - DB에는 문자열로 저장 (예: "ROLE_USER")
 */
public enum Role {
    /**
     * 일반 사용자 권한 (기본값)
     * - 회원가입 시 자동으로 부여
     * - 자신의 일기 CRUD 가능
     */
    ROLE_USER("일반 사용자"),

    /**
     * 관리자 권한
     * - 모든 사용자 관리 가능
     * - 시스템 설정 변경 가능
     */
    ROLE_ADMIN("관리자");

    private final String description;

    /**
     * Enum 생성자
     * @param description 권한 설명 (UI 표시용)
     */
    Role(String description) {
        this.description = description;
    }

    /**
     * 권한 설명 반환
     * @return 한글 설명 (예: "일반 사용자")
     */
    public String getDescription() {
        return description;
    }
}