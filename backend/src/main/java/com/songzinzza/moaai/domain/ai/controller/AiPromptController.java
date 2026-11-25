package com.songzinzza.moaai.domain.ai.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.ai.dto.PromptRefineRequest;
import com.songzinzza.moaai.domain.ai.dto.PromptRefineResponse;
import com.songzinzza.moaai.domain.ai.service.PromptRefineService;
import com.songzinzza.moaai.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "AI Prompt Refine", description = "Gemini 기반 프롬프트 개선 API")
@RestController
@RequestMapping("/api/ai/prompts/refine")
@RequiredArgsConstructor
public class AiPromptController {
  private final PromptRefineService promptRefineService;

  @Operation(summary = "프롬프트 자동 개선", description = "Gemini를 활용해 입력한 프롬프트를 구조화하고 개선 (일일 호출 횟수 제한 적용)")
  @PostMapping
  public ApiResponse<PromptRefineResponse> refine(
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody PromptRefineRequest req) {
    return ApiResponse.ok(promptRefineService.refine(userId, req));
  }

  @Operation(summary = "오늘 남은 프롬프트 개선 횟수 조회")
  @GetMapping("/remaining")
  public ApiResponse<Long> getRemainingCount(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(promptRefineService.getRemainingCount(userId));
  }
}
