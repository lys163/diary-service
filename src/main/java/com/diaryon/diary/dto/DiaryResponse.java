package com.diaryon.diary.dto;

import com.diaryon.diary.entity.Mood;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일기 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDate diaryDate;
    private Mood mood;
    private String moodEmoji;
    private String moodDescription;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
