package com.diaryon.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 비밀번호 확인 요청 DTO
 * - 회원 탈퇴 등 중요한 작업 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordConfirmRequest {
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
