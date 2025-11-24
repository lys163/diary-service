package com.diaryon.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 비밀번호 변경 요청 DTO
 * - 기존 비밀번호 확인 필수 (보안)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeRequest {
    @NotBlank(message = "기존 비밀번호는 필수입니다")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자 사이여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다")
    private String newPassword;
}
