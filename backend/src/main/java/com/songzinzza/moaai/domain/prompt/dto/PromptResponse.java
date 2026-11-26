package com.songzinzza.moaai.domain.prompt.dto;

import java.time.LocalDateTime;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;

public record PromptResponse(
    Long id,
    Long authorId,
    String authorNickname,
    String title,
    String content,
    String resultExample,
    PromptCategory category,
    AiModel aiModel,
    long likeCount,
    long bookmarkCount,
    long copyCount,
    boolean liked,
    boolean bookmarked,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static PromptResponse of(Prompt prompt, boolean liked, boolean bookmarked) {
    return new PromptResponse(
        prompt.getId(),
        prompt.getUser().getId(),
        prompt.getUser().getNickname(),
        prompt.getTitle(),
        prompt.getContent(),
        prompt.getResultExample(),
        prompt.getCategory(),
        prompt.getAiModel(),
        prompt.getLikeCount(),
        prompt.getBookmarkCount(),
        prompt.getCopyCount(),
        liked,
        bookmarked,
        prompt.getCreatedAt(),
        prompt.getUpdatedAt());
  }

  public PromptResponse withUserStatus(boolean liked, boolean bookmarked) {
    return new PromptResponse(
        this.id,
        this.authorId,
        this.authorNickname,
        this.title,
        this.content,
        this.resultExample,
        this.category,
        this.aiModel,
        this.likeCount,
        this.bookmarkCount,
        this.copyCount,
        liked,
        bookmarked,
        this.createdAt,
        this.updatedAt);
  }
}
