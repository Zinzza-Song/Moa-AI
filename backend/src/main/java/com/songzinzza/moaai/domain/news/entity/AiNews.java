package com.songzinzza.moaai.domain.news.entity;

import java.time.LocalDateTime;

import com.songzinzza.moaai.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_news", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ai_news_origianl_url", columnNames = { "original_url" })
}, indexes = {
    @Index(name = "idx_ai_news_published_at", columnList = "published_at")
})
public class AiNews extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "original_url", nullable = false, length = 500)
  private String originalUrl;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String summary;

  @Column(length = 255)
  private String tags; // 쉼표 구분 태그 (ex) "LLM,Google,Gemini")

  @Column(nullable = false, length = 100)
  private String sourceName;

  private LocalDateTime publishedAt;

  @Builder
  public AiNews(String originalUrl, String title, String summary, String tags,
      String sourceName, LocalDateTime publishedAt) {
    this.originalUrl = originalUrl;
    this.title = title;
    this.summary = summary;
    this.tags = tags;
    this.sourceName = sourceName;
    this.publishedAt = publishedAt;
  }
}
