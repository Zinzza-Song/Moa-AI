package com.songzinzza.moaai.domain.ai.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.songzinzza.moaai.domain.ai.dto.PromptRefineRequest;
import com.songzinzza.moaai.domain.ai.dto.PromptRefineResponse;
import com.songzinzza.moaai.domain.ai.service.PromptRefineService.AiRefineOutput;
import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PromptRefineServiceTest {

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ChatClient chatClient;

  @Mock
  private AiRateLimitService aiRateLimitService;

  private PromptRefineService promptRefineService;

  @BeforeEach
  void setUp() {
    given(chatClientBuilder.build()).willReturn(chatClient);

    promptRefineService = new PromptRefineService(chatClientBuilder, aiRateLimitService);
  }

  @Test
  @DisplayName("프롬프트 개선 성공 시 추천 제목, 개선 프롬프트, 실행 결과 예시, 남은 횟수를 반환")
  void refine_success_returnsTitleAndResultExample() {
    PromptRefineRequest req = new PromptRefineRequest("쇼츠 대본 써줘", AiModel.values()[0], "재미있게");

    AiRefineOutput aiOutput = new AiRefineOutput(
        "유튜브 쇼츠 밈 해설 대본 프롬프트",
        "[역할] 인기 쇼츠 작가...",
        "[화면 연출] 0~5초: 강렬한 훅 자막...",
        List.of("역할 부여 추가", "시간대별 출력 형식 지정"));

    given(aiRateLimitService.checkAndIncrement(1L)).willReturn(9L);
    given(chatClient.prompt().system(anyString()).user(anyString()).call().entity(AiRefineOutput.class))
        .willReturn(aiOutput);

    PromptRefineResponse result = promptRefineService.refine(1L, req);

    assertThat(result.suggestedTitle()).isEqualTo("유튜브 쇼츠 밈 해설 대본 프롬프트");
    assertThat(result.refinedPrompt()).isEqualTo("[역할] 인기 쇼츠 작가...");
    assertThat(result.resultExample()).isEqualTo("[화면 연출] 0~5초: 강렬한 훅 자막...");
    assertThat(result.improvements()).hasSize(2);
    assertThat(result.remainingDailyCount()).isEqualTo(9L);
  }

  @Test
  @DisplayName("Gemini 호출 실패 시 차감된 일일 사용량을 롤백하고 AI_API_ERROR 예외 발생")
  void refine_geminiError_rollsBackRateLimit() {
    PromptRefineRequest req = new PromptRefineRequest("코드 리뷰해줘", AiModel.values()[0], null);

    given(aiRateLimitService.checkAndIncrement(1L)).willReturn(9L);
    given(chatClient.prompt().system(anyString()).user(anyString()).call().entity(AiRefineOutput.class))
        .willThrow(new RuntimeException("Gemini timeout"));

    assertThatThrownBy(() -> promptRefineService.refine(1L, req))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.AI_API_ERROR);

    verify(aiRateLimitService).rollback(1L);
  }
}
