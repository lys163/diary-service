package com.diaryon.diary.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 에러 응답 표준 형식
 *
 * 모든 API 에러는 이 형식으로 통일하여 반환
 * - 클라이언트가 에러를 일관되게 처리 가능
 * - 프론트엔드에서 에러 처리 로직 단순화
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL):
 * - null 값인 필드는 JSON에 포함하지 않음
 * - validationErrors가 null이면 응답에서 제외
 *
 * 응답 예시:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "이미 존재하는 사용자명입니다",
 *   "path": "/api/auth/signup"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    /**
     * 에러 발생 시각
     * ISO-8601 형식: "2024-01-15T10:30:00"
     */
    private LocalDateTime timestamp;

    /**
     * HTTP 상태 코드
     * 예: 400, 401, 403, 404, 500
     */
    private int status;

    /**
     * HTTP 상태 메시지
     * 예: "Bad Request", "Unauthorized", "Not Found"
     */
    private String error;

    /**
     * 사용자에게 보여줄 에러 메시지
     * - 한글로 작성
     * - 구체적이고 이해하기 쉽게
     *
     * 예시:
     * - "사용자명은 3~50자 사이여야 합니다"
     * - "해당 일기에 접근 권한이 없습니다"
     */
    private String message;

    /**
     * 에러가 발생한 API 경로 (선택사항)
     * 예: "/api/auth/signup"
     *
     * 디버깅 시 유용하지만 보안상 노출하지 않는 것도 고려
     */
    private String path;

    /**
     * 유효성 검증 실패 시 필드별 에러 메시지
     * - @Valid 검증 실패 시에만 포함
     * - null이면 JSON에 포함되지 않음
     *
     * 예시:
     * {
     *   "username": "사용자명은 필수입니다",
     *   "email": "올바른 이메일 형식이 아닙니다",
     *   "password": "비밀번호는 8~100자 사이여야 합니다"
     * }
     */
    private Map<String, String> validationErrors;
}
