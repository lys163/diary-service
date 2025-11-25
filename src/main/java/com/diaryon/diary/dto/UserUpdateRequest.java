package com.diaryon.diary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * 사용자 정보 수정 요청 DTO
 * - 모든 필드 선택사항 (변경할 필드만 전송)
 * - username은 변경 불가 (Primary Key)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다")
    @Max(value = 150, message = "나이는 150 이하여야 합니다")
    private Integer age;

    @Size(min=3, max=12,message = "닉네임은 3글자 이상 12글자 이하로 입력해야 합니다.")
    private String nickname;
}
