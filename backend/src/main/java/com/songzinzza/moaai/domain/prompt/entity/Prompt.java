package com.songzinzza.moaai.domain.prompt.entity;

import com.songzinzza.moaai.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prompts")
public class Prompt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(columnDefinition = "TEXT")
  private String resultExample;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PromptCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AiModel aiModel;

  @Column(nullable = false)
  private long likeCount = 0L;

  @Column(nullable = false)
  private long bookmarkCount = 0L;

  @Column(nullable = false)
  private long copyCount = 0L;

  @Builder
  public Prompt(User user, String title, String content, String resultExample,
      PromptCategory category, AiModel aiModel) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.resultExample = resultExample;
    this.category = category;
    this.aiModel = aiModel;
  }

  public void update(String title, String content, String resultExample,
      PromptCategory category, AiModel aiModel) {
    this.title = title;
    this.content = content;
    this.resultExample = resultExample;
    this.category = category;
    this.aiModel = aiModel;
  }

  public void increaseLike() {
    this.likeCount++;
  }

  public void decreaseLilke() {
    if (this.likeCount > 0)
      this.likeCount--;
  }

  public void increaseBookmark() {
    this.bookmarkCount++;
  }

  public void decreaseBookmark() {
    if (this.bookmarkCount > 0)
      this.bookmarkCount--;
  }

  public void increaseCopy() {
    this.copyCount++;
  }
}
