package com.diaryon.diary.service;

import com.diaryon.diary.dto.DiaryCalendarResponse;
import com.diaryon.diary.dto.DiaryCreateRequest;
import com.diaryon.diary.dto.DiaryResponse;
import com.diaryon.diary.dto.DiaryUpdateRequest;
import com.diaryon.diary.entity.Diary;
import com.diaryon.diary.entity.User;
import com.diaryon.diary.repostitory.DiaryRepository;
import com.diaryon.diary.repostitory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    /**
     * 일기 작성
     */
    @Transactional
    public DiaryResponse createDiary(String username, DiaryCreateRequest request) {
        // 1. 사용자 조회
        User user = findUserByUsername(username);

        // 2. 같은 날짜에 일기가 이미 있는지 체크
        LocalDate diaryDate = request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now();
        if (diaryRepository.findByUserAndDiaryDate(user, diaryDate).isPresent()) {
            throw new IllegalArgumentException("해당 날짜에 이미 일기가 존재합니다: " + diaryDate);
        }

        // 3. Diary 엔티티 생성
        Diary diary = Diary.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .diaryDate(diaryDate)
                .mood(request.getMood())
                .build();

        // 4. 저장
        Diary savedDiary = diaryRepository.save(diary);
        log.info("일기 작성 완료: diaryId={}, username={}", savedDiary.getId(), username);

        return convertToResponse(savedDiary);
    }

    /**
     * 내 일기 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<DiaryResponse> getMyDiaries(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        Page<Diary> diaries = diaryRepository.findByUser(user, pageable);
        return diaries.map(this::convertToResponse);
    }

    /**
     * 일기 상세 조회
     */
    @Transactional(readOnly = true)
    public DiaryResponse getDiary(String username, Long diaryId) {
        Diary diary = findDiaryById(diaryId);
        validateOwner(username, diary);
        return convertToResponse(diary);
    }

    /**
     * 일기 수정
     */
    @Transactional
    public DiaryResponse updateDiary(String username, Long diaryId, DiaryUpdateRequest request) {
        Diary diary = findDiaryById(diaryId);
        validateOwner(username, diary);

        // 엔티티 수정 (Dirty Checking)
        // Diary 엔티티에 update 메서드 추가 필요
        diary = Diary.builder()
                .id(diary.getId())
                .user(diary.getUser())
                .title(request.getTitle() != null ? request.getTitle() : diary.getTitle())
                .content(request.getContent() != null ? request.getContent() : diary.getContent())
                .mood(request.getMood() != null ? request.getMood() : diary.getMood())
                .diaryDate(diary.getDiaryDate())
                .createdAt(diary.getCreatedAt())
                .build();

        Diary updatedDiary = diaryRepository.save(diary);
        log.info("일기 수정 완료: diaryId={}", diaryId);
        return convertToResponse(updatedDiary);
    }

    /**
     * 일기 삭제
     */
    @Transactional
    public void deleteDiary(String username, Long diaryId) {
        Diary diary = findDiaryById(diaryId);
        validateOwner(username, diary);
        diaryRepository.delete(diary);
        log.info("일기 삭제 완료: diaryId={}", diaryId);
    }

    /**
     * 특정 날짜의 일기 조회
     */
    @Transactional(readOnly = true)
    public DiaryResponse getDiaryByDate(String username, String dateStr) {
        User user = findUserByUsername(username);
        LocalDate date = LocalDate.parse(dateStr);
        Diary diary = diaryRepository.findByUserAndDiaryDate(user, date)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜에 일기가 없습니다: " + dateStr));
        return convertToResponse(diary);
    }

    /**
     * 월별 일기 조회 (달력용)
     * - 해당 월의 1일~말일까지 모든 날짜에 대한 일기 정보 반환
     */
    @Transactional(readOnly = true)
    public DiaryCalendarResponse getMonthlyDiaries(String username, int year, int month) {
        User user = findUserByUsername(username);

        // 1. 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        int totalDays = yearMonth.lengthOfMonth();

        // 2. 해당 월의 모든 일기 조회
        List<Diary> diaries = diaryRepository.findByUserAndDateRange(user, startDate, endDate);

        // 3. 날짜별로 Map으로 변환 (빠른 검색을 위해)
        Map<LocalDate, Diary> diaryMap = diaries.stream()
                .collect(Collectors.toMap(Diary::getDiaryDate, diary -> diary));

        // 4. 1일부터 말일까지 모든 날짜에 대한 정보 생성
        List<DiaryCalendarResponse.DailyDiary> dailyDiaries = new ArrayList<>();
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            Diary diary = diaryMap.get(date);

            DiaryCalendarResponse.DailyDiary dailyDiary;
            if (diary != null) {
                // 일기가 있는 경우
                dailyDiary = DiaryCalendarResponse.DailyDiary.builder()
                        .date(date)
                        .hasDiary(true)
                        .diaryId(diary.getId())
                        .title(diary.getTitle())
                        .mood(diary.getMood())
                        .moodEmoji(diary.getMood() != null ? diary.getMood().getEmoji() : null)
                        .build();
            } else {
                // 일기가 없는 경우
                dailyDiary = DiaryCalendarResponse.DailyDiary.builder()
                        .date(date)
                        .hasDiary(false)
                        .diaryId(null)
                        .title(null)
                        .mood(null)
                        .moodEmoji(null)
                        .build();
            }
            dailyDiaries.add(dailyDiary);
        }

        // 5. 응답 생성
        return DiaryCalendarResponse.builder()
                .year(year)
                .month(month)
                .totalDays(totalDays)
                .diaryCount(diaries.size())
                .dailyDiaries(dailyDiaries)
                .build();
    }

    // ===== Private Helper Methods =====

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    private Diary findDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다: " + diaryId));
    }

    private void validateOwner(String username, Diary diary) {
        if (!diary.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("해당 일기에 접근 권한이 없습니다");
        }
    }

    private DiaryResponse convertToResponse(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .diaryDate(diary.getDiaryDate())
                .mood(diary.getMood())
                .moodEmoji(diary.getMood() != null ? diary.getMood().getEmoji() : null)
                .moodDescription(diary.getMood() != null ? diary.getMood().getDescription() : null)
                .username(diary.getUser().getUsername())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
