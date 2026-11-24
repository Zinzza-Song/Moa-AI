package com.songzinzza.moaai.domain.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.songzinzza.moaai.domain.auth.dto.TokenResponse;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;
import com.songzinzza.moaai.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final JwtProvider jwtProvider;
  private final StringRedisTemplate redisTemplate;

  public TokenResponse reissue(String refreshToken) {
    if (!jwtProvider.validateToken(refreshToken))
      throw new BusinessException(ErrorCode.INVALID_TOKEN);

    Long userId = jwtProvider.getUserId(refreshToken);
    String role = jwtProvider.getRole(refreshToken);
    String redisKey = "refresh:" + userId;

    String savedToken = redisTemplate.opsForValue().get(redisKey);
    if (savedToken == null || !savedToken.equals(refreshToken))
      throw new BusinessException(ErrorCode.INVALID_TOKEN);

    // Refresh Token Rotation (기존 토큰 폐기 후 새 쌍 발급)
    String newAccessToken = jwtProvider.createAccessToken(userId, role);
    String newRefreshToken = jwtProvider.createRefreshToken(userId, role);

    redisTemplate.opsForValue().set(
        redisKey,
        newRefreshToken,
        Duration.ofMillis(jwtProvider.getRefreshExpiration()));

    return new TokenResponse(newAccessToken, newRefreshToken);
  }

  public void logout(Long userId) {
    redisTemplate.delete("refresh:" + userId);
  }
}
