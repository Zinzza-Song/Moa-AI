package com.songzinzza.moaai.domain.prompt.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class PromptRankingServiceTest {
  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ZSetOperations<String, String> zSetOperations;

  @InjectMocks
  private PromptRankingService promptRankingService;

  @Test
  @DisplayName("인기 점수 추가 시 Redis ZSET에 해당 promptId 점수를 누적")
  void addScore_incrementsZSetScore() {
    given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

    promptRankingService.addScore(10L, PromptRankingService.LIKE_SCORE);

    verify(zSetOperations).incrementScore(PromptRankingService.POPULAR_RANKING_KEY, "10", 3.0);
  }

  @Test
  @DisplayName("상위 인기 프롬프트 ID 목록을 점수 내림차순으로 반환")
  void getTopPromptIds_returnsOrderedIds() {
    Set<String> orderedSet = new LinkedHashSet<>(List.of("30", "10", "20"));
    given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
    given(zSetOperations.reverseRange(PromptRankingService.POPULAR_RANKING_KEY, 0, 4L))
        .willReturn(orderedSet);

    List<Long> topIds = promptRankingService.getTopPromptIds(5);

    assertThat(topIds).containsExactly(30L, 10L, 20L);
  }

  @Test
  @DisplayName("Redis 예외 발생 시 예외를 전파하지 않고 빈 목록을 반환해 Fallback을 유도")
  void getTopPromptIds_returnsEmptyOnRedisError() {
    given(redisTemplate.opsForZSet()).willThrow(new RuntimeException("Redis down"));

    List<Long> topIds = promptRankingService.getTopPromptIds(5);

    assertThat(topIds).isEmpty();
  }
}
