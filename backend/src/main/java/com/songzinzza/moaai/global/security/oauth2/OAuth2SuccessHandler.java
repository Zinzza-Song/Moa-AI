package com.songzinzza.moaai.global.security.oauth2;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.songzinzza.moaai.domain.auth.dto.TokenResponse;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.global.common.ApiResponse;
import com.songzinzza.moaai.global.security.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
  private final JwtProvider jwtProvider;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
      Authentication authentication) throws IOException {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    User user = oAuth2User.getUser();

    String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().getKey());
    String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().getKey());

    // Redis에 Refresh Token 저장(키: refresh:{userId})
    redisTemplate.opsForValue().set(
        "refresh:" + user.getId(),
        refreshToken,
        Duration.ofMillis(jwtProvider.getRefreshExpiration()));

    // // 추후 React 프론트엔드 연동 시 프론트 콜백 URL로 리다이렉트 변경 예정
    // res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    // res.setCharacterEncoding("UTF-8");
    // TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
    // res.getWriter().write(objectMapper.writeValueAsString(ApiResponse.ok(tokenResponse)));

    String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback")
        .queryParam("accessToken", accessToken)
        .queryParam("refreshToken", refreshToken)
        .build()
        .toUriString();

    res.sendRedirect(targetUrl);
  }
}
