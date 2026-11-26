package com.songzinzza.moaai.domain.prompt.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.prompt.dto.PromptCreateRequest;
import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.prompt.dto.PromptToggleResponse;
import com.songzinzza.moaai.domain.prompt.dto.PromptUpdateRequest;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptBookmark;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.entity.PromptLike;
import com.songzinzza.moaai.domain.prompt.repository.PromptBookmarkRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptLikeRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.config.RedisCacheConfig;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromptService {
  private final PromptRepository promptRepository;
  private final PromptLikeRepository promptLikeRepository;
  private final PromptBookmarkRepository promptBookmarkRepository;
  private final UserRepository userRepository;
  private final PromptCacheService promptCacheService;

  @Transactional
  @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true)
  public PromptResponse create(Long userId, PromptCreateRequest req) {
    User user = findUser(userId);

    Prompt prompt = Prompt.builder()
        .user(user)
        .title(req.title())
        .content(req.content())
        .resultExample(req.resultExample())
        .category(req.category())
        .aiModel(req.aiModel())
        .build();

    Prompt saved = promptRepository.save(prompt);

    return PromptResponse.of(saved, false, false);
  }

  public PageResponse<PromptResponse> getList(
      PromptCategory category,
      AiModel aiModel,
      String keyword,
      Pageable pageable,
      Long currentUserId) {
    PageResponse<PromptResponse> cached = promptCacheService.getPublicList(category, aiModel, keyword, pageable);

    if (currentUserId == null || cached.content().isEmpty())
      return cached;

    List<Long> promptIds = cached.content().stream().map(PromptResponse::id).toList();
    Set<Long> likedIds = promptLikeRepository.findLikedPromptIds(currentUserId, promptIds);
    Set<Long> bookmarkedIds = promptBookmarkRepository.findBookmarkedPromptIds(currentUserId, promptIds);

    return cached.map(p -> p.withUserStatus(likedIds.contains(p.id()), bookmarkedIds.contains(p.id())));
  }

  public PromptResponse getDetail(Long promptId, Long currentUserId) {
    PromptResponse cached = promptCacheService.getPublicDetail(promptId);

    if (currentUserId == null)
      return cached;

    boolean liked = promptLikeRepository.existsByUserIdAndPromptId(currentUserId, promptId);
    boolean bookmarked = promptBookmarkRepository.existsByUserIdAndPromptId(currentUserId, promptId);

    return cached.withUserStatus(liked, bookmarked);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  })
  public PromptResponse update(Long promptId, Long userId, PromptUpdateRequest req) {
    Prompt prompt = findPromptWithUser(promptId);

    validateOwner(prompt, userId);

    prompt.update(
        req.title(),
        req.content(),
        req.resultExample(),
        req.category(),
        req.aiModel());

    boolean liked = promptLikeRepository.existsByUserIdAndPromptId(userId, promptId);
    boolean bookmarked = promptBookmarkRepository.existsByUserIdAndPromptId(userId, promptId);

    return PromptResponse.of(prompt, liked, bookmarked);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  })
  public void delete(Long promptId, Long userId) {
    Prompt prompt = findPromptWithUser(promptId);

    validateOwner(prompt, userId);

    promptLikeRepository.deleteByPromptId(promptId);
    promptBookmarkRepository.deleteByPromptId(promptId);
    promptRepository.delete(prompt);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  })
  public PromptToggleResponse toggleLike(Long promptId, Long userId) {
    Prompt prompt = findPrompt(promptId);

    User user = findUser(userId);

    return promptLikeRepository.findByUserIdAndPromptId(userId, promptId)
        .map(existing -> {
          promptLikeRepository.delete(existing);
          prompt.decreaseLilke();

          return new PromptToggleResponse(false, prompt.getLikeCount());
        })
        .orElseGet(() -> {
          promptLikeRepository.save(new PromptLike(user, prompt));
          prompt.increaseLike();

          return new PromptToggleResponse(true, prompt.getLikeCount());
        });
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  })
  public PromptToggleResponse toggleBookmark(Long promptId, Long userId) {
    Prompt prompt = findPrompt(promptId);

    User user = findUser(userId);

    return promptBookmarkRepository.findByUserIdAndPromptId(userId, promptId)
        .map(existing -> {
          promptBookmarkRepository.delete(existing);
          prompt.decreaseBookmark();

          return new PromptToggleResponse(false, prompt.getBookmarkCount());
        })
        .orElseGet(() -> {
          promptBookmarkRepository.save(new PromptBookmark(user, prompt));
          prompt.increaseBookmark();

          return new PromptToggleResponse(true, prompt.getBookmarkCount());
        });
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, key = "#promptId")
  })
  public long increaseCopyCount(Long promptId) {
    Prompt prompt = findPrompt(promptId);
    prompt.increaseCopy();

    return prompt.getCopyCount();
  }

  private User findUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  private Prompt findPrompt(Long promptId) {
    return promptRepository.findById(promptId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROMPT_NOT_FOUND));
  }

  private Prompt findPromptWithUser(Long promptId) {
    return promptRepository.findWithUserById(promptId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROMPT_NOT_FOUND));
  }

  private void validateOwner(Prompt prompt, Long userId) {
    if (!prompt.getUser().getId().equals(userId))
      throw new BusinessException(ErrorCode.NOT_PROMPT_OWNER);
  }
}
