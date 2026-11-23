package com.songzinzza.moaai.domain.prompt.entity;

import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.global.common.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prompt_likes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_prompt_like_user_prompt", columnNames = { "user_id", "prompt_id" })
})
public class PromptLike extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prompt_id", nullable = false)
  private Prompt prompt;

  public PromptLike(User user, Prompt prompt) {
    this.user = user;
    this.prompt = prompt;
  }
}
