package com.songzinzza.moaai.domain.news.dto;

import java.time.LocalDateTime;

import com.songzinzza.moaai.domain.news.entity.AiVideo;

public record AiVideoResponse(
    Long id,
    String videoId,
    String title,
    String channelTitle,
    String thumbnailUrl,
    LocalDateTime publishedAt,
    String videoUrl) {
  public static AiVideoResponse from(AiVideo video) {
    return new AiVideoResponse(
        video.getId(),
        video.getVideoId(),
        video.getTitle(),
        video.getChannelTitle(),
        video.getThumbnailUrl(),
        video.getPublishedAt(),
        "https://www.youtube.com/watch?v=" + video.getVideoId());
  }
}
