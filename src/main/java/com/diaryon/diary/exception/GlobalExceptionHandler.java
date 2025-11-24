package com.diaryon.diary.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
/**
 * 전역 예외 처리 핸들러
 *
 * @RestControllerAdvice:
 * - 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
 * - @ControllerAdvice + @ResponseBody의 조합
 * - JSON 형태의 에러 응답 반환
 *
 * 장점:
 * 1. 일관된 에러 응답 형식
 * 2. 컨트롤러 코드 간결화 (try-catch 불필요)
 * 3. 로깅 중앙화
 * 4. 클라이언트 친화적인 에러 메시지
 *
 * 예외 처리 우선순위:
 * 1. 구체적인 예외 (@ExceptionHandler)
 * 2. 일반적인 예외 (Exception)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // ========== 인증/인가 관련 예외 ==========

    /**
     * 인증 실패 (로그인 실패)
     *
     * 발생 시점:
     * - 로그인 시 사용자명 또는 비밀번호가 틀렸을 때
     * - AuthenticationManager.authenticate() 실패
     *
     * HTTP 상태: 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        log.warn("인증 실패: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("사용자명 또는 비밀번호가 올바르지 않습니다")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * 사용자를 찾을 수 없음
     *
     * 발생 시점:
     * - CustomUserDetailsService에서 사용자 조회 실패
     * - 존재하지 않는 사용자명으로 로그인 시도
     *
     * HTTP 상태: 404 Not Found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException e) {
        log.warn("사용자 조회 실패: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("사용자를 찾을 수 없습니다")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 접근 권한 없음
     *
     * 발생 시점:
     * - @PreAuthorize 권한 체크 실패
     * - 일반 사용자가 관리자 전용 API 호출
     *
     * HTTP 상태: 403 Forbidden
     *
     * 예시:
     * - ROLE_USER가 /api/admin/** 접근 시도
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("해당 리소스에 접근할 권한이 없습니다")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ========== 유효성 검증 예외 ==========

    /**
     * 요청 데이터 유효성 검증 실패
     *
     * 발생 시점:
     * - @Valid 검증 실패
     * - DTO의 @NotBlank, @Email, @Size 등 위반
     *
     * HTTP 상태: 400 Bad Request
     *
     * 응답 형식:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "유효성 검증 실패",
     *   "validationErrors": {
     *     "username": "사용자명은 필수입니다",
     *     "email": "올바른 이메일 형식이 아닙니다"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());

        // 필드별 에러 메시지 수집
        Map<String, String> validationErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("유효성 검증 실패")
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ========== 비즈니스 로직 예외 ==========

    /**
     * 잘못된 요청 (IllegalArgumentException)
     *
     * 발생 시점:
     * - 중복된 사용자명/이메일 가입 시도
     * - 이미 존재하는 날짜에 일기 작성
     * - 잘못된 파라미터 전달
     * - 비밀번호 불일치
     * - 본인 소유가 아닌 일기 수정/삭제 시도
     *
     * HTTP 상태: 400 Bad Request
     *
     * 예시:
     * - "이미 존재하는 사용자명입니다"
     * - "해당 날짜에 이미 일기가 존재합니다"
     * - "해당 일기에 접근 권한이 없습니다"
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 리소스를 찾을 수 없음 (RuntimeException)
     *
     * 발생 시점:
     * - 존재하지 않는 일기 ID로 조회
     * - 존재하지 않는 사용자 ID로 조회
     *
     * HTTP 상태: 404 Not Found
     *
     * 참고: IllegalArgumentException 대신 별도의
     * ResourceNotFoundException을 만들어 사용하는 것이 더 좋음
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        // RuntimeException은 범위가 넓으므로 메시지에 따라 분기
        if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
            log.warn("리소스 조회 실패: {}", e.getMessage());

            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // 그 외 RuntimeException은 일반 예외 핸들러로 전달
        return handleException(e);
    }

    // ========== 기타 예외 ==========

    /**
     * 예상하지 못한 모든 예외
     *
     * 발생 시점:
     * - 위에서 처리하지 못한 모든 예외
     * - NullPointerException, IllegalStateException 등
     *
     * HTTP 상태: 500 Internal Server Error
     *
     * 주의:
     * - 실무에서는 상세한 에러 메시지를 클라이언트에 노출하지 않음
     * - 에러 ID를 생성하여 로그와 매칭
     * - 모니터링 시스템(Sentry, CloudWatch)에 전송
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상하지 못한 에러 발생", e);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
