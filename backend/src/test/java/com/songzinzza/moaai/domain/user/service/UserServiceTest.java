package com.songzinzza.moaai.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.songzinzza.moaai.domain.prompt.dto.PromptResponse;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.repository.PromptBookmarkRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptLikeRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.domain.user.dto.MyPageSummaryResponse;
import com.songzinzza.moaai.domain.user.entity.Role;
import com.songzinzza.moaai.domain.user.entity.SocialProvider;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.storage.ImageStorageService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PromptRepository promptRepository;

  @Mock
  private PromptLikeRepository promptLikeRepository;

  @Mock
  private PromptBookmarkRepository promptBookmarkRepository;

  @Mock
  private ImageStorageService imageStorageService;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("내 활동 요약 조회 시 작성·좋아요·북마크 개수를 반환")
  void getMySummary_returnsCounts() {
    given(promptRepository.countByUserId(1L)).willReturn(5L);
    given(promptLikeRepository.countByUserId(1L)).willReturn(12L);
    given(promptBookmarkRepository.countByUserId(1L)).willReturn(8L);

    MyPageSummaryResponse summary = userService.getMySummary(1L);

    assertThat(summary.writtenPromptCount()).isEqualTo(5L);
    assertThat(summary.likedPromptCount()).isEqualTo(12L);
    assertThat(summary.bookmarkedPromptCount()).isEqualTo(8L);
  }

  @Test
  @DisplayName("내가 작성한 프롬프트 목록 조회 시 좋아요/북마크 상태를 오버레이")
  void getMyPrompts_overlaysStatus() {
    Pageable pageable = PageRequest.of(0, 12);
    User user = createUser(1L);
    Prompt p1 = createPrompt(10L, user);
    Prompt p2 = createPrompt(20L, user);

    given(promptRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
        .willReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));
    given(promptLikeRepository.findLikedPromptIds(1L, List.of(10L, 20L))).willReturn(Set.of(10L));
    given(promptBookmarkRepository.findBookmarkedPromptIds(1L, List.of(10L, 20L))).willReturn(Set.of(20L));

    PageResponse<PromptResponse> result = userService.getMyPrompts(1L, pageable);

    assertThat(result.content()).hasSize(2);
    assertThat(result.content().get(0).liked()).isTrue();
    assertThat(result.content().get(0).bookmarked()).isFalse();
    assertThat(result.content().get(1).liked()).isFalse();
    assertThat(result.content().get(1).bookmarked()).isTrue();
  }

  @Test
  @DisplayName("내가 좋아요한 프롬프트 조회 시 liked=true로 고정하고 북마크 상태만 조회")
  void getMyLikedPrompts_setsLikedTrueAndChecksBookmarksOnly() {
    Pageable pageable = PageRequest.of(0, 12);
    User author = createUser(2L);
    Prompt p1 = createPrompt(10L, author);

    given(promptLikeRepository.findLikedPromptsByUserId(1L, pageable))
        .willReturn(new PageImpl<>(List.of(p1), pageable, 1));
    given(promptBookmarkRepository.findBookmarkedPromptIds(1L, List.of(10L))).willReturn(Set.of(10L));

    PageResponse<PromptResponse> result = userService.getMyLikedPrompts(1L, pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).liked()).isTrue();
    assertThat(result.content().get(0).bookmarked()).isTrue();
    verify(promptLikeRepository, never()).findLikedPromptIds(any(), any());
  }

  @Test
  @DisplayName("내가 북마크한 프롬프트 조회 시 bookmarked=true로 고정하고 좋아요 상태만 조회")
  void getMyBookmarkedPrompts_setsBookmarkedTrueAndChecksLikesOnly() {
    Pageable pageable = PageRequest.of(0, 12);
    User author = createUser(2L);
    Prompt p1 = createPrompt(10L, author);

    given(promptBookmarkRepository.findBookmarkedPromptsByUserId(1L, pageable))
        .willReturn(new PageImpl<>(List.of(p1), pageable, 1));
    given(promptLikeRepository.findLikedPromptIds(1L, List.of(10L))).willReturn(Set.of());

    PageResponse<PromptResponse> result = userService.getMyBookmarkedPrompts(1L, pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).liked()).isFalse();
    assertThat(result.content().get(0).bookmarked()).isTrue();
    verify(promptBookmarkRepository, never()).findBookmarkedPromptIds(any(), any());
  }

  private User createUser(Long id) {
    User user = User.builder()
        .email("user" + id + "@moaai.com")
        .nickname("유저" + id)
        .provider(SocialProvider.values()[0])
        .providerId("provider-" + id)
        .role(Role.USER)
        .build();
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private Prompt createPrompt(Long id, User user) {
    Prompt prompt = Prompt.builder()
        .user(user)
        .title("프롬프트 " + id)
        .content("내용")
        .resultExample("예시")
        .category(PromptCategory.values()[0])
        .aiModel(AiModel.values()[0])
        .build();
    ReflectionTestUtils.setField(prompt, "id", id);
    return prompt;
  }

  @Test
  @DisplayName("닉네임 수정 시 중복이 아니면 정상 변경")
  void updateProfile_success() {
    User user = createUser(1L);

    given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
    given(userRepository.existsByNicknameAndIdNot("새닉네임", 1L)).willReturn(false);

    var response = userService.updateProfile(1L,
        new com.songzinzza.moaai.domain.user.dto.UserProfileUpdateRequest("새닉네임"));

    assertThat(response.nickname()).isEqualTo("새닉네임");
  }

  @Test
  @DisplayName("이미 사용 중인 닉네임으로 수정 시 DUPLICATE_NICKNAME 예외가 발생")
  void updateProfile_duplicateNickname_throwsException() {
    User user = createUser(1L);

    given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
    given(userRepository.existsByNicknameAndIdNot("중복닉네임", 1L)).willReturn(true);

    org.assertj.core.api.Assertions.assertThatThrownBy(
        () -> userService.updateProfile(1L, new com.songzinzza.moaai.domain.user.dto.UserProfileUpdateRequest("중복닉네임")))
        .isInstanceOf(com.songzinzza.moaai.global.exception.BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(com.songzinzza.moaai.global.exception.ErrorCode.DUPLICATE_NICKNAME);
  }

  @Test
  @DisplayName("프로필 이미지 변경 시 새 이미지를 저장하고 기존 이미지를 삭제")
  void updateProfileImage_replacesOldImage() {
    User user = createUser(1L);
    user.updateProfileImageUrl("/uploads/profiles/old.png");

    var file = new org.springframework.mock.web.MockMultipartFile(
        "file", "new.png", "image/png", "dummy".getBytes());

    given(userRepository.findById(1L)).willReturn(java.util.Optional.of(user));
    given(imageStorageService.store(file, "profiles")).willReturn("/uploads/profiles/new.png");

    var response = userService.updateProfileImage(1L, file);

    assertThat(response.profileImageUrl()).isEqualTo("/uploads/profiles/new.png");
    verify(imageStorageService).delete("/uploads/profiles/old.png");
  }
}
