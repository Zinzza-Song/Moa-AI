package com.songzinzza.moaai.domain.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.user.dto.MyPageSummaryResponse;
import com.songzinzza.moaai.domain.user.dto.UserResponse;
import com.songzinzza.moaai.domain.user.service.UserService;
import com.songzinzza.moaai.global.common.ApiResponse;
import com.songzinzza.moaai.global.common.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "회원 정보 API")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회")
  @GetMapping()
  public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(userService.getMyInfo(userId));
  }

  @Operation(summary = "내 활동 요약 조회", description = "작성 좋아요 북마크한 프롬프트 개수 조회")
  @GetMapping("/summary")
  public ApiResponse<MyPageSummaryResponse> getMySummary(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(userService.getMySummary(userId));
  }

  @Operation(summary = "내가 작성한 프롬프트 목록 조회")
  @GetMapping("/prompts")
  public ApiResponse<PageResponse<PromptResponse>> getMyPrompts(
      @AuthenticationPrincipal Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    Pageable pageable = PageRequest.of(page, size);

    return ApiResponse.ok(userService.getMyPrompts(userId, pageable));
  }

  @Operation(summary = "내가 북마크한 프롬프트 목록 조회")
  @GetMapping("/likes")
  public ApiResponse<PageResponse<PromptResponse>> getMyLikedPrompts(
      @AuthenticationPrincipal Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    Pageable pageable = PageRequest.of(page, size);

    return ApiResponse.ok(userService.getMyLikedPrompts(userId, pageable));
  }

  @Operation(summary = "내가 북마크한 프롬프트 목록 조회")
  @GetMapping("/bookmarks")
  public ApiResponse<PageResponse<PromptResponse>> getMyBookmarkedPrompts(
      @AuthenticationPrincipal Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    Pageable pageable = PageRequest.of(page, size);

    return ApiResponse.ok(userService.getMyBookmarkedPrompts(userId, pageable));
  }
}
