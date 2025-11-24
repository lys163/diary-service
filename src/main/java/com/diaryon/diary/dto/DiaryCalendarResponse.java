package com.diaryon.diary.dto;


import com.diaryon.diary.entity.Mood;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 월별 달력용 응답 DTO
 * - 해당 월의 모든 일기 정보를 날짜별로 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryCalendarResponse {
    private int year;
    private int month;
    private int totalDays;              // 해당 월의 총 일수 (28~31)
    private int diaryCount;             // 작성된 일기 개수
    private List<DailyDiary> dailyDiaries;  // 날짜별 일기 정보
    /**
     * 날짜별 일기 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDiary {
        private LocalDate date;         // 날짜 (2024-01-15)
        private boolean hasDiary;       // 일기 존재 여부
        private Long diaryId;           // 일기 ID (없으면 null)
        private String title;           // 일기 제목 (없으면 null)
        private Mood mood;              // 기분 상태 (없으면 null)
        private String moodEmoji;       // 기분 이모지 (없으면 null)
    }
}
