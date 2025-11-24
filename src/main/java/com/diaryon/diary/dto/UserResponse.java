package com.diaryon.diary.dto;

import com.diaryon.diary.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 사용자 정보 응답 DTO
 * - 비밀번호는 절대 포함하지 않음 (보안)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private Integer age;
    private Set<Role> roles;
    private Long diaryCount;  // 작성한 일기 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
