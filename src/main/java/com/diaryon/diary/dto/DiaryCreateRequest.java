package com.diaryon.diary.dto;

import com.diaryon.diary.entity.Mood;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

/**
 * 일기 작성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryCreateRequest {
//    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

//    @NotBlank(message = "내용은 필수입니다")
    private String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;  // null이면 오늘 날짜로 설정

    private Mood mood;  // 기분 상태 (선택)
}
