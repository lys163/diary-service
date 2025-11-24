package com.diaryon.diary.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * 사용자 통계 응답 DTO (관리자 전용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {
    private Long totalUsers;           // 전체 사용자 수
    private Long todaySignups;         // 오늘 가입한 사용자 수
    private Long thisMonthSignups;     // 이번 달 가입한 사용자 수
    private Long adminCount;           // 관리자 수
    private Long userCount;            // 일반 사용자 수
    private LocalDate lastSignupDate;  // 마지막 가입일
}
