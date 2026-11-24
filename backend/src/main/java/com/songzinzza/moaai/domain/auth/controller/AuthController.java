package com.songzinzza.moaai.domain.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.auth.dto.TokenReissueRequest;
import com.songzinzza.moaai.domain.auth.dto.TokenResponse;
import com.songzinzza.moaai.domain.auth.service.AuthService;
import com.songzinzza.moaai.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag (name = "Auth", description = "인증 및 토큰 관리 API")
@RestController 
@RequestMapping ("/api/auth")
@RequiredArgsConstructor 
public class AuthController {
  private final AuthService authService;

  @Operation (summary = "토큰 재발급", description = "만료된 Access Token 대신 Refresh Token으로 새 토큰 쌍을 발급")
  @PostMapping("/reissue")
  public ApiResponse<TokenResponse> reissue(@Valid @RequestBody TokenReissueRequest req) {
      return ApiResponse.ok(authService.reissue(req.refreshToken()));
  }

  @Operation (summary = "로그아웃", description = "Redis에 저장된 Refresh Token을 삭제")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
      authService.logout(userId);
      
      return ApiResponse.ok();
  }
  
  
}
