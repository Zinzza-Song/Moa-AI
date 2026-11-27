package com.songzinzza.moaai.domain.user.dto;

public record MyPageSummaryResponse(
    long writtenPromptCount,
    long likedPromptCount,
    long bookmarkedPromptCount) {

}
