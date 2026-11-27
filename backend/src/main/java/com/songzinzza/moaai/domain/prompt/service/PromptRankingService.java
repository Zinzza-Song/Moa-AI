package com.songzinzza.moaai.domain.prompt.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptRankingService {
  public static final String POPULAR_RANKING_KEY = "prompt:ranking:popular";

  public static final double LIKE_SCORE = 3.0;
  public static final double BOOKMARK_SCORE = 2.0;
  public static final double COPY_SCORE = 2.0;

  private final StringRedisTemplate redisTemplate;

  public void addScore(Long promptId, double delta) {
    try {
      redisTemplate.opsForZSet().incrementScore(POPULAR_RANKING_KEY, String.valueOf(promptId), delta);
    } catch (Exception e) {
      log.warn("Redis 인기 랭킹 점수 갱신 실패 - promptId{}, delta={}", promptId, delta, e);
    }
  }

  public void removePrompt(Long promptId) {
    try {
      redisTemplate.opsForZSet().remove(POPULAR_RANKING_KEY, String.valueOf(promptId));
    } catch (Exception e) {
      log.warn("Redis 인기 랭킹 항목 제거 실패 - promptId{}", promptId, e);
    }
  }

  public List<Long> getTopPromptIds(int limit) {
    try {
      Set<String> rawIds = redisTemplate.opsForZSet()
          .reverseRange(POPULAR_RANKING_KEY, 0, limit - 1L);

      if (rawIds == null || rawIds.isEmpty())
        return Collections.emptyList();

      return rawIds.stream()
          .map(Long::valueOf)
          .toList();
    } catch (Exception e) {
      log.warn("Redis 인기 랭킹 조회 실패 - DB Fallback으로 전환", e);

      return Collections.emptyList();
    }
  }
}
