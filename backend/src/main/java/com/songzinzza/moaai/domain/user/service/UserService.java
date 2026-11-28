package com.songzinzza.moaai.domain.user.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.repository.PromptBookmarkRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptLikeRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.domain.user.dto.MyPageSummaryResponse;
import com.songzinzza.moaai.domain.user.dto.UserProfileUpdateRequest;
import com.songzinzza.moaai.domain.user.dto.UserResponse;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.config.RedisCacheConfig;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;
import com.songzinzza.moaai.global.storage.ImageStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final PromptRepository promptRepository;
  private final PromptLikeRepository promptLikeRepository;
  private final PromptBookmarkRepository promptBookmarkRepository;
  private final ImageStorageService imageStorageService;

  public UserResponse getMyInfo(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return UserResponse.from(user);
  }

  public MyPageSummaryResponse getMySummary(Long userId) {
    long writtenCount = promptRepository.countByUserId(userId);
    long likedCount = promptLikeRepository.countByUserId(userId);
    long bookmarkedCount = promptBookmarkRepository.countByUserId(userId);

    return new MyPageSummaryResponse(writtenCount, likedCount, bookmarkedCount);
  }

  public PageResponse<PromptResponse> getMyPrompts(Long userId, Pageable pageable) {
    Page<Prompt> page = promptRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

    if (page.isEmpty())
      return PageResponse.from(page.map(p -> PromptResponse.of(p, false, false)));

    List<Long> promtIds = page.getContent().stream().map(Prompt::getId).toList();
    Set<Long> likedIds = promptLikeRepository.findLikedPromptIds(userId, promtIds);
    Set<Long> bookmarkedIds = promptBookmarkRepository.findBookmarkedPromptIds(userId, promtIds);

    return PageResponse.from(page.map(
        p -> PromptResponse.of(p, likedIds.contains(p.getId()), bookmarkedIds.contains(p.getId()))));
  }

  public PageResponse<PromptResponse> getMyLikedPrompts(Long userId, Pageable pageable) {
    Page<Prompt> page = promptLikeRepository.findLikedPromptsByUserId(userId, pageable);

    if (page.isEmpty())
      return PageResponse.from(page.map(p -> PromptResponse.of(p, false, false)));

    List<Long> promptIds = page.getContent().stream().map(Prompt::getId).toList();
    Set<Long> bookmarkedIds = promptBookmarkRepository.findBookmarkedPromptIds(userId, promptIds);

    return PageResponse.from(page.map(
        p -> PromptResponse.of(p, true, bookmarkedIds.contains(p.getId()))));
  }

  public PageResponse<PromptResponse> getMyBookmarkedPrompts(Long userId, Pageable pageable) {
    Page<Prompt> page = promptBookmarkRepository.findBookmarkedPromptsByUserId(userId, pageable);

    if (page.isEmpty())
      return PageResponse.from(page.map(p -> PromptResponse.of(p, false, false)));

    List<Long> promptIds = page.getContent().stream().map(Prompt::getId).toList();
    Set<Long> likedIds = promptLikeRepository.findLikedPromptIds(userId, promptIds);

    return PageResponse.from(page.map(
        p -> PromptResponse.of(p, likedIds.contains(p.getId()), true)));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = RedisCacheConfig.PROMPT_LIST, allEntries = true),
      @CacheEvict(value = RedisCacheConfig.PROMPT_DETAIL, allEntries = true)
  })
  public UserResponse updateProfile(Long userId, UserProfileUpdateRequest req) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    String trimmedNickname = req.nickname().trim();
    if (userRepository.existsByNicknameAndIdNot(trimmedNickname, userId))
      throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);

    user.updateNickname(trimmedNickname);

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateProfileImage(Long userId, MultipartFile file) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    String oldImageUrl = user.getProfileImageUrl();
    String newImageUrl = imageStorageService.store(file, "profiles");

    user.updateProfileImageUrl(newImageUrl);

    if (oldImageUrl != null)
      imageStorageService.delete(oldImageUrl);

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse deleteProfileImage(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    String oldImageUrl = user.getProfileImageUrl();

    user.updateProfileImageUrl(null);

    if (oldImageUrl != null)
      imageStorageService.delete(oldImageUrl);

    return UserResponse.from(user);
  }
}
