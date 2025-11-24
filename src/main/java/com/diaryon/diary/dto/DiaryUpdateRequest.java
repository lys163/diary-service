package com.diaryon.diary.dto;

import com.diaryon.diary.entity.Mood;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 일기 수정 요청 DTO
 * - 모든 필드 선택사항 (변경할 필드만 전송)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryUpdateRequest {
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    private String content;

    private Mood mood;
}
