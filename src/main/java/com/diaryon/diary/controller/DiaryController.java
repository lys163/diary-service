package com.diaryon.diary.controller;

import com.diaryon.diary.dto.DiaryCalendarResponse;
import com.diaryon.diary.dto.DiaryCreateRequest;
import com.diaryon.diary.dto.DiaryResponse;
import com.diaryon.diary.dto.DiaryUpdateRequest;
import com.diaryon.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 일기 작성
     * POST /api/diaries
     */
    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody DiaryCreateRequest request) {
        log.info("일기 작성 요청: username={}, title={}", username, request.getTitle());
        DiaryResponse response = diaryService.createDiary(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 일기 목록 조회 (페이징)
     * GET /api/diaries?page=0&size=10&sort=diaryDate,desc
     */
    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getMyDiaries(
            @AuthenticationPrincipal String username,
            @PageableDefault(size = 10, sort = "diaryDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("내 일기 목록 조회: username={}", username);
        Page<DiaryResponse> diaries = diaryService.getMyDiaries(username, pageable);
        return ResponseEntity.ok(diaries);
    }

    /**
     * 일기 상세 조회
     * GET /api/diaries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiary(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        log.info("일기 상세 조회: username={}, diaryId={}", username, id);
        DiaryResponse response = diaryService.getDiary(username, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 일기 수정
     * PUT /api/diaries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiaryResponse> updateDiary(
            @AuthenticationPrincipal String username,
            @PathVariable Long id,
            @Valid @RequestBody DiaryUpdateRequest request) {
        log.info("일기 수정 요청: username={}, diaryId={}", username, id);
        DiaryResponse response = diaryService.updateDiary(username, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 일기 삭제
     * DELETE /api/diaries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        log.info("일기 삭제 요청: username={}, diaryId={}", username, id);
        diaryService.deleteDiary(username, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 날짜의 일기 조회
     * GET /api/diaries/date/2024-01-15
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<DiaryResponse> getDiaryByDate(
            @AuthenticationPrincipal String username,
            @PathVariable String date) {
        log.info("날짜별 일기 조회: username={}, date={}", username, date);
        DiaryResponse response = diaryService.getDiaryByDate(username, date);
        return ResponseEntity.ok(response);
    }

    /**
     * 월별 일기 조회 (달력용)
     * GET /api/diaries/calendar/2024/1
     *
     * 해당 월의 1일~말일까지 일기 존재 여부와 기분 상태를 반환
     */
    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<DiaryCalendarResponse> getMonthlyDiaries(
            @AuthenticationPrincipal String username,
            @PathVariable int year,
            @PathVariable int month) {
        log.info("월별 일기 조회: username={}, year={}, month={}", username, year, month);
        DiaryCalendarResponse response = diaryService.getMonthlyDiaries(username, year, month);
        return ResponseEntity.ok(response);
    }

}
