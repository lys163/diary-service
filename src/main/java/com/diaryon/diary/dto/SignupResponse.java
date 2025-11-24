package com.diaryon.diary.dto;

import lombok.*;

/**
 * 회원가입 응답 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupResponse {
    private Long userid;
    private String username;
    private String email;
    private String message;
}
