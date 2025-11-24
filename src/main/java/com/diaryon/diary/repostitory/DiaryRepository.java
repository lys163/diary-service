package com.diaryon.diary.repostitory;

import com.diaryon.diary.entity.Diary;
import com.diaryon.diary.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary,Long> {

    /**
     * 특정 사용자의 일기 목록 조회 (페이징)
     */
    Page<Diary> findByUser(User user, Pageable pageable);

    /**
     * 특정 사용자의 특정 날짜 일기 조회
     */
    Optional<Diary> findByUserAndDiaryDate(User user, LocalDate diaryDate);

    /**
     * 특정 사용자의 특정 기간 일기 조회 (달력용)
     * - startDate 이상, endDate 이하인 일기 조회
     */
    @Query("SELECT d FROM Diary d WHERE d.user = :user AND d.diaryDate BETWEEN :startDate AND :endDate ORDER BY d.diaryDate")
    List<Diary> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 사용자의 일기 개수
     */
    long countByUser(User user);

    /**
     * 특정 사용자의 특정 월 일기 개수
     */
    @Query("SELECT COUNT(d) FROM Diary d WHERE d.user = :user AND YEAR(d.diaryDate) = :year AND MONTH(d.diaryDate) = :month")
    long countByUserAndYearMonth(
            @Param("user") User user,
            @Param("year") int year,
            @Param("month") int month
    );

}
