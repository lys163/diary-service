package com.diaryon.diary.entity;

/**
 * 일기 작성 시 기분 상태 Enum
 * - UI에 이모지와 함께 표시
 * - DB에는 문자열로 저장 (예: "HAPPY")
 */
public enum Mood {
    HAPPY("😄", "행복함"),
    LOVE("\uD83E\uDD70", "사랑"),
    NEUTRAL("😐", "보통"),
    SAD("😢", "슬픔"),
    ANGRY("😠", "화남");

    private final String emoji;
    private final String description;

    /**
     * Enum 생성자
     *
     * @param emoji       이모지 (UI 표시용)
     * @param description 기분 설명 (한글)
     */
    Mood(String emoji, String description) {
        this.emoji = emoji;
        this.description = description;
    }

    /**
     * 이모지 반환
     *
     * @return 이모지 문자 (예: "😊")
     */
    public String getEmoji() {
        return emoji;
    }

    /**
     * 기분 설명 반환
     *
     * @return 한글 설명 (예: "행복함")
     */
    public String getDescription() {
        return description;
    }
}