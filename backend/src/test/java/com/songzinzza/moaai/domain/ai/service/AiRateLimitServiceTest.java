package com.songzinzza.moaai.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AiRateLimitServiceTest {
  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private AiRateLimitService aiRateLimitService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(aiRateLimitService, "dailyLimit", 10L);
  }

  @Test
  @DisplayName("오늘 첫 요청 시 자정 TTL을 설정하고 남은 횟수를 반환")
  void checkAndIncrement_firstRequest_setsExpire() {
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.increment(anyString())).willReturn(1L);

    long remaining = aiRateLimitService.checkAndIncrement(1L);

    assertThat(remaining).isEqualTo(9L);
    verify(redisTemplate).expire(anyString(), any(Duration.class));
  }

  @Test
  @DisplayName("일일 한도(10회)를 초과하면 AI_RATE_LIMIT_EXCEEDED 예외가 발생")
  void checkAndIncrement_exceedsLimit_throwsException() {
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.increment(anyString())).willReturn(11L);

    assertThatThrownBy(() -> aiRateLimitService.checkAndIncrement(1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
  }
}
