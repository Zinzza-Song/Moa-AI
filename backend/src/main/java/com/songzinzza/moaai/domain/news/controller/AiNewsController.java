package com.songzinzza.moaai.domain.news.controller;

import org.springframework.web.bind.annotation.RestController;

import com.songzinzza.moaai.domain.news.dto.AiNewsCreateRequest;
import com.songzinzza.moaai.domain.news.dto.AiNewsResponse;
import com.songzinzza.moaai.domain.news.service.AiNewsService;
import com.songzinzza.moaai.global.common.ApiResponse;
import com.songzinzza.moaai.global.common.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI News", description = "AI 트렌드 뉴스 API")
@RestController
@RequiredArgsConstructor
public class AiNewsController {
  private final AiNewsService aiNewsService;

  @Operation(summary = "AI 뉴스 목록 조회 및 검색", description = "키워드, 태그 필터링 및 최신 발행순 페이징 조회 지원")
  @GetMapping("/api/news")
  public ApiResponse<PageResponse<AiNewsResponse>> getList(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

    return ApiResponse.ok(aiNewsService.getList(keyword, tag, pageable));
  }

  @Operation(summary = "AI 뉴스 상세 조회")
  @GetMapping("/api/news/{newsId}")
  public ApiResponse<AiNewsResponse> getDetail(@PathVariable Long newsId) {
    return ApiResponse.ok(aiNewsService.getDetail(newsId));
  }

  @Operation(summary = "[관리자] AI 뉴스 등록", description = "Admin 권한 계정만 등록 가능")
  @PostMapping("/api/admin/news")
  public ApiResponse<AiNewsResponse> create(@Valid @RequestBody AiNewsCreateRequest req) {
    return ApiResponse.ok(aiNewsService.create(req));
  }

  @Operation(summary = "[관리자] AI 뉴스 삭제", description = "Admin 권한 계정만 삭제 가능")
  @DeleteMapping("/api/admin/news/{newsId}")
  public ApiResponse<Void> delete(@PathVariable Long newsId) {
    aiNewsService.delete(newsId);

    return ApiResponse.ok();
  }
}
