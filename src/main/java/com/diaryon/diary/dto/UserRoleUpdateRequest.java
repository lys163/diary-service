package com.diaryon.diary.dto;

import com.diaryon.diary.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

/**
 * 사용자 권한 변경 요청 DTO (관리자 전용)
 * - 관리자가 특정 사용자의 권한을 변경할 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequest {
    @NotEmpty(message = "권한은 최소 1개 이상이어야 합니다")
    private Set<Role> roles;
}
