package com.songzinzza.moaai.domain.prompt.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.config.RedisCacheConfig;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromptCacheService {
  private final PromptRepository promptRepository;

  @Cacheable(value = RedisCacheConfig.PROMPT_LIST, key = "(#category != null ? #category.name() : 'all') + ':' "
      + "+ (#aiModel != null ? #aiModel.name() : 'all') + ':' "
      + "+ #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()", condition = "#keyword == null || #keyword.isBlank()")
  public PageResponse<PromptResponse> getPublicList(
      PromptCategory category,
      AiModel aiModel,
      String keyword,
      Pageable pageable) {
    Page<PromptResponse> mapped = promptRepository.searchPrompts(category, aiModel, keyword, pageable)
        .map(p -> PromptResponse.of(p, false, false));

    return PageResponse.from(mapped);
  }

  @Cacheable(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  public PromptResponse getPublicDetail(Long promptId) {
    Prompt prompt = promptRepository.findWithUserById(promptId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROMPT_NOT_FOUND));

    return PromptResponse.of(prompt, false, false);
  }
}
