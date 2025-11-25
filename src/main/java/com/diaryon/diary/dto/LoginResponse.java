package com.diaryon.diary.dto;

import lombok.*;

import java.util.Set;

/**
 * 로그인 응답 DTO
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String nickname;
    private Long userId;
    private String username;
    private String email;
    private Set<String> roles;
}
