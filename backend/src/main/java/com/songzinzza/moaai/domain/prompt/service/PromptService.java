package com.songzinzza.moaai.domain.prompt.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
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

  @Transactional
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
    Page<Prompt> page = promptRepository.searchPrompts(category, aiModel, keyword, pageable);

    List<Long> promptIds = page.getContent().stream().map(Prompt::getId).toList();

    Set<Long> likedIds = (currentUserId != null && !promptIds.isEmpty())
        ? promptLikeRepository.findLikedPromptIds(currentUserId, promptIds)
        : Collections.emptySet();

    Set<Long> bookmarkedIds = (currentUserId != null && !promptIds.isEmpty())
        ? promptBookmarkRepository.findBookmarkedPromptIds(currentUserId, promptIds)
        : Collections.emptySet();

    Page<PromptResponse> mapped = page
        .map(p -> PromptResponse.of(p, likedIds.contains(p.getId()), bookmarkedIds.contains(p.getId())));

    return PageResponse.from(mapped);
  }

  public PromptResponse getDetail(Long promptId, Long currentUserId) {
    Prompt prompt = findPromptWithUser(promptId);

    boolean liked = currentUserId != null && promptLikeRepository.existsByUserIdAndPromptId(currentUserId, promptId);
    boolean bookmarked = currentUserId != null
        && promptBookmarkRepository.existsByUserIdAndPromptId(currentUserId, promptId);

    return PromptResponse.of(prompt, liked, bookmarked);
  }

  @Transactional
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
  public void delete(Long promptId, Long userId) {
    Prompt prompt = findPromptWithUser(promptId);

    validateOwner(prompt, userId);

    promptLikeRepository.deleteByPromptId(promptId);
    promptBookmarkRepository.deleteByPromptId(promptId);
    promptRepository.delete(prompt);
  }

  @Transactional
  public PromptToggleResponse toogleLike(Long promptId, Long userId) {
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
