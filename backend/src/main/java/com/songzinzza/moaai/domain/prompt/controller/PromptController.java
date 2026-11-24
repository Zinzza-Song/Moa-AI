package com.songzinzza.moaai.domain.prompt.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.prompt.dto.PromptCreateRequest;
import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.prompt.dto.PromptToggleResponse;
import com.songzinzza.moaai.domain.prompt.dto.PromptUpdateRequest;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.service.PromptService;
import com.songzinzza.moaai.global.common.ApiResponse;
import com.songzinzza.moaai.global.common.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Prompt", description = "프롬프트 공유 및 상호작용 API")
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {
  private final PromptService promptService;

  @Operation(summary = "프롬프트 등록", description = "새로운 프롬프트를 등록합니다. (로그인 필요)")
  @PostMapping
  public ApiResponse<PromptResponse> create(
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody PromptCreateRequest req) {
    return ApiResponse.ok(promptService.create(userId, req));
  }

  @Operation(summary = "프롬프트 목록 조회 및 검색", description = "카테고리, AI 모델, 키워드 필터링 및 정렬(createdAt, likeCount, copyCount)을 지원")
  @GetMapping
  public ApiResponse<PageResponse<PromptResponse>> getList(
      @RequestParam(required = false) PromptCategory category,
      @RequestParam(required = false) AiModel aiModel,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @AuthenticationPrincipal Long currentUserId) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

    return ApiResponse.ok(promptService.getList(category, aiModel, keyword, pageable, currentUserId));
  }

  @Operation(summary = "프롬프트 상세 조회")
  @GetMapping("/{promptId}")
  public ApiResponse<PromptResponse> getDetail(
      @PathVariable Long promptId,
      @AuthenticationPrincipal Long currentUserId) {
    return ApiResponse.ok(promptService.getDetail(promptId, currentUserId));
  }

  @Operation(summary = "프롬프트 수정", description = "작성자 본인만 수정 가능")
  @PutMapping("/{promptId}")
  public ApiResponse<PromptResponse> update(
      @PathVariable Long promptId,
      @AuthenticationPrincipal Long userId,
      @Valid @RequestBody PromptUpdateRequest req) {
    return ApiResponse.ok(promptService.update(promptId, userId, req));
  }

  @Operation(summary = "프롬프트 삭제", description = "작성자 본인만 삭제 가능")
  @DeleteMapping("/{promptId}")
  public ApiResponse<Void> delete(
      @PathVariable Long promptId,
      @AuthenticationPrincipal Long userId) {
    promptService.delete(promptId, userId);

    return ApiResponse.ok();
  }

  @Operation(summary = "프롬프트 좋아요 토글", description = "이미 좋아요 상태면 취소, 아니면 추가")
  @PostMapping("/{promptId}/like")
  public ApiResponse<PromptToggleResponse> toggleLike(
      @PathVariable Long promptId,
      @AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(promptService.toggleLike(promptId, userId));
  }

  @Operation(summary = "프롬프트  북마크 토글", description = "이미 북마크 상태면 취소, 아니면 추가")
  @PostMapping("/{promptId}/bookmark")
  public ApiResponse<PromptToggleResponse> toggleBookmark(
      @PathVariable Long promptId,
      @AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(promptService.toggleBookmark(promptId, userId));
  }

  @Operation(summary = "프롬프트 복사 카운트 증가", description = "프롬프트 복사 버튼 클릭 시 호출")
  @PostMapping("/{promptId}/copy")
  public ApiResponse<Long> increaseCopyCount(@PathVariable Long promptId) {
    return ApiResponse.ok(promptService.increaseCopyCount(promptId));
  }
}
