package com.songzinzza.moaai.domain.news.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.news.dto.AiNewsCreateRequest;
import com.songzinzza.moaai.domain.news.dto.AiNewsResponse;
import com.songzinzza.moaai.domain.news.entity.AiNews;
import com.songzinzza.moaai.domain.news.repository.AiNewsRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiNewsService {
  private final AiNewsRepository aiNewsRepository;

  public PageResponse<AiNewsResponse> getList(String keyword, String tag, Pageable pageable) {
    Page<AiNewsResponse> page = aiNewsRepository.searchNews(keyword, tag, pageable)
        .map(AiNewsResponse::from);
    return PageResponse.from(page);
  }

  public AiNewsResponse getDetail(Long newId) {
    AiNews news = aiNewsRepository.findById(newId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));

    return AiNewsResponse.from(news);
  }

  @Transactional
  public AiNewsResponse create(AiNewsCreateRequest req) {
    if (aiNewsRepository.existsByOriginalUrl(req.originalUrl()))
      throw new BusinessException(ErrorCode.DUPLICATE_NEWS_URL);

    AiNews news = AiNews.builder()
        .title(req.title())
        .summary(req.summary())
        .originalUrl(req.originalUrl())
        .sourceName(req.sourceName())
        .tags(req.tags())
        .publishedAt(req.publishedAt() != null ? req.publishedAt() : LocalDateTime.now())
        .build();

    return AiNewsResponse.from(aiNewsRepository.save(news));
  }

  @Transactional
  public void delete(Long newId) {
    AiNews news = aiNewsRepository.findById(newId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));

    aiNewsRepository.delete(news);
  }
}
