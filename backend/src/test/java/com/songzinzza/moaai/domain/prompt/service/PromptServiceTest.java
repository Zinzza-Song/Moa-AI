package com.songzinzza.moaai.domain.prompt.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import com.songzinzza.moaai.domain.prompt.dto.PromptToggleResponse;
import com.songzinzza.moaai.domain.prompt.dto.PromptUpdateRequest;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.entity.PromptLike;
import com.songzinzza.moaai.domain.prompt.repository.PromptBookmarkRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptLikeRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.domain.user.entity.Role;
import com.songzinzza.moaai.domain.user.entity.SocialProvider;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;
import com.songzinzza.moaai.global.common.PageResponse;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {
  @Mock
  private PromptRepository promptRepository;

  @Mock
  private PromptLikeRepository promptLikeRepository;

  @Mock
  private PromptBookmarkRepository promptBookmarkRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PromptCacheService promptCacheService;

  @InjectMocks
  private PromptService promptService;

  @Test
  @DisplayName("로그인 유저가 목록 조회 시 공용 캐시에 개인화 좋아요/북마크 상태를 오버레이")
  void getList_loggedInUser_overlayUserStatus() {
    Pageable pageable = PageRequest.of(0, 10);
    PromptResponse p1 = createPromptResponse(1L);
    PromptResponse p2 = createPromptResponse(2L);
    PageResponse<PromptResponse> cached = PageResponse.from(new PageImpl<>(List.of(p1, p2), pageable, 2));

    given(promptCacheService.getPublicList(null, null, null, pageable)).willReturn(cached);
    given(promptLikeRepository.findLikedPromptIds(10L, List.of(1L, 2L))).willReturn(Set.of(1L));
    given(promptBookmarkRepository.findBookmarkedPromptIds(10L, List.of(1L, 2L))).willReturn(Set.of(2L));

    PageResponse<PromptResponse> result = promptService.getList(null, null, null, pageable, 10L);

    assertThat(result.content()).hasSize(2);
    assertThat(result.content().get(0).liked()).isTrue();
    assertThat(result.content().get(0).bookmarked()).isFalse();
    assertThat(result.content().get(1).liked()).isFalse();
    assertThat(result.content().get(1).bookmarked()).isTrue();
  }

  @Test
  @DisplayName("비로그인 유저가 목록 조회 시 좋아요/북마크 DB 조회 없이 캐시를 바로 반환")
  void getList_anonymousUser_skipsUserStatusQueries() {
    Pageable pageable = PageRequest.of(0, 10);
    PageResponse<PromptResponse> cached = PageResponse.from(
        new PageImpl<>(List.of(createPromptResponse(1L)), pageable, 1));

    given(promptCacheService.getPublicList(null, null, null, pageable)).willReturn(cached);

    PageResponse<PromptResponse> result = promptService.getList(null, null, null, pageable, null);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).liked()).isFalse();
    verify(promptLikeRepository, never()).findLikedPromptIds(any(), any());
    verify(promptBookmarkRepository, never()).findBookmarkedPromptIds(any(), any());
  }

  @Test
  @DisplayName("작성자가 아닌 유저가 프롬프트를 수정하면 NOT_PROMPT_OWNER 예외가 발생")
  void update_notOwner_throwsException() {
    User owner = createUser(1L);
    Prompt prompt = createPrompt(100L, owner);
    PromptUpdateRequest req = new PromptUpdateRequest(
        "수정 제목", "수정 내용", "예시", PromptCategory.values()[0], AiModel.values()[0]);

    given(promptRepository.findWithUserById(100L)).willReturn(Optional.of(prompt));

    assertThatThrownBy(() -> promptService.update(100L, 999L, req))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_PROMPT_OWNER);
  }

  @Test
  @DisplayName("작성자가 아닌 유저가 프롬프트를 삭제하면 NOT_PROMPT_OWNER 예외가 발생")
  void delete_notOwner_throwsException() {
    User owner = createUser(1L);
    Prompt prompt = createPrompt(100L, owner);

    given(promptRepository.findWithUserById(100L)).willReturn(Optional.of(prompt));

    assertThatThrownBy(() -> promptService.delete(100L, 999L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NOT_PROMPT_OWNER);

    verify(promptRepository, never()).delete(any());
  }

  @Test
  @DisplayName("좋아요 토글 시 기록이 없으면 추가하고 카운트를 1 증가")
  void toggleLike_addsLikeWhenNotExists() {
    User user = createUser(1L);
    Prompt prompt = createPrompt(100L, user);

    given(promptRepository.findById(100L)).willReturn(Optional.of(prompt));
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(promptLikeRepository.findByUserIdAndPromptId(1L, 100L)).willReturn(Optional.empty());

    PromptToggleResponse res = promptService.toggleLike(100L, 1L);

    assertThat(res.active()).isTrue();
    assertThat(res.count()).isEqualTo(1L);
    verify(promptLikeRepository).save(any(PromptLike.class));
  }

  @Test
  @DisplayName("좋아요 토글 시 이미 기록이 있으면 삭제하고 카운트를 1 감소")
  void toggleLike_removesLikeWhenExists() {
    User user = createUser(1L);
    Prompt prompt = createPrompt(100L, user);
    prompt.increaseLike();
    PromptLike existingLike = new PromptLike(user, prompt);

    given(promptRepository.findById(100L)).willReturn(Optional.of(prompt));
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(promptLikeRepository.findByUserIdAndPromptId(1L, 100L)).willReturn(Optional.of(existingLike));

    PromptToggleResponse res = promptService.toggleLike(100L, 1L);

    assertThat(res.active()).isFalse();
    assertThat(res.count()).isEqualTo(0L);
    verify(promptLikeRepository).delete(existingLike);
  }

  private User createUser(Long id) {
    User user = User.builder()
        .email("test@moaai.com")
        .nickname("테스터")
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
        .title("테스트 프롬프트")
        .content("프롬프트 본문")
        .resultExample("결과 예시")
        .category(PromptCategory.values()[0])
        .aiModel(AiModel.values()[0])
        .build();

    ReflectionTestUtils.setField(prompt, "id", id);

    return prompt;
  }

  private PromptResponse createPromptResponse(Long id) {
    return new PromptResponse(
        id, 1L, "테스터", "제목 " + id, "내용", "예시",
        PromptCategory.values()[0], AiModel.values()[0],
        0L, 0L, 0L, false, false,
        LocalDateTime.now(), LocalDateTime.now());
  }
}
