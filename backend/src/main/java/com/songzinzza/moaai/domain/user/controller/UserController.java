package com.songzinzza.moaai.domain.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.user.dto.UserResponse;
import com.songzinzza.moaai.domain.user.service.UserService;
import com.songzinzza.moaai.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "User", description = "회원 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회")
  @GetMapping("/me")
  public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(userService.getMyInfo(userId));
  }

}
