package com.songzinzza.moaai.domain.news.entity;

import java.time.LocalDateTime;

import com.songzinzza.moaai.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_videos")
public class AiVideo extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "video_id", nullable = false, unique = true, length = 50)
  private String videoId; // 유튜브 고유 Video ID

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 100)
  private String channelTitle;

  @Column(length = 500)
  private String thumbnailUrl;

  @Column(nullable = false)
  private LocalDateTime publishedAt;

  @Builder
  public AiVideo(String videoId, String title, String channelTitle, String thumbnailUrl, LocalDateTime publishedAt) {
    this.videoId = videoId;
    this.title = title;
    this.channelTitle = channelTitle;
    this.thumbnailUrl = thumbnailUrl;
    this.publishedAt = publishedAt;
  }
}
