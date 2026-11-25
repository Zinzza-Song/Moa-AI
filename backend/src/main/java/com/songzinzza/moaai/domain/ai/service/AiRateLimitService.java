package com.songzinzza.moaai.domain.ai.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiRateLimitService {
  private static final String KEY_PREFIX = "ai:rate-limit:refine:";
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final StringRedisTemplate redisTemplate;

  @Value("${ai.rate-limit.daily-limit:10}")
  private long dailyLimit;

  // 일일 사용량 증가 및 한도 검증 후 남은 횟수 반환
  public long checkAndIncrement(Long userId) {
    String key = buildKey(userId);
    Long currentCount = redisTemplate.opsForValue().increment(key);

    if (currentCount == null)
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

    // 오늘 첫 요청인 경우 다음 날 자정(KST)까지 만료 시간(TTL) 설정
    if (currentCount == 1L)
      redisTemplate.expire(key, durationUntilMidnight());

    if (currentCount > dailyLimit)
      throw new BusinessException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);

    return Math.max(0L, dailyLimit - currentCount);
  }

  // 외부 AI API 호출 실패 시 차감된 횟수 1회 복구
  public void rollback(Long userId) {
    String key = buildKey(userId);

    Long decremented = redisTemplate.opsForValue().decrement(key);

    if (decremented != null && decremented < 0L)
      redisTemplate.opsForValue().set(key, "0", durationUntilMidnight());
  }

  // 현재 사용자의 오늘 남은 요청 횟수 조회
  public long getRemainingCount(Long userId) {
    String value = redisTemplate.opsForValue().get(buildKey(userId));

    long used = (value == null) ? 0L : Long.parseLong(value);

    return Math.max(0L, dailyLimit - used);
  }

  private String buildKey(Long userId) {
    return KEY_PREFIX + userId + ":" + LocalDate.now(KST);
  }

  private Duration durationUntilMidnight() {
    ZonedDateTime now = ZonedDateTime.now(KST);
    ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(KST);

    return Duration.between(now, nextMidnight);
  }
}
