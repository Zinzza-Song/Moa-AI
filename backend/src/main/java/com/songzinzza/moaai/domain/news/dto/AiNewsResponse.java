package com.songzinzza.moaai.domain.news.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.songzinzza.moaai.domain.news.entity.AiNews;

public record AiNewsResponse(
    Long id,
    String title,
    String summary,
    String originalUrl,
    String sourceName,
    List<String> tags,
    LocalDateTime publishedAt,
    LocalDateTime createdAt) {
  public static AiNewsResponse from(AiNews news) {
    List<String> tagList = (news.getTags() == null || news.getTags().isBlank())
        ? Collections.emptyList()
        : Arrays.stream(news.getTags().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

    return new AiNewsResponse(
        news.getId(),
        news.getTitle(),
        news.getSummary(),
        news.getOriginalUrl(),
        news.getSourceName(),
        tagList,
        news.getPublishedAt(),
        news.getCreatedAt());
  }
}
