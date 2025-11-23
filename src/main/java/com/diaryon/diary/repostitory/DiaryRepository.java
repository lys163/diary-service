package com.diaryon.diary.repostitory;

import com.diaryon.diary.entity.Diary;
import com.diaryon.diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary,Long> {

}
