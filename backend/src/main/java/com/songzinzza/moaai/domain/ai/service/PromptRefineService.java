package com.songzinzza.moaai.domain.ai.service;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.songzinzza.moaai.domain.ai.dto.PromptRefineRequest;
import com.songzinzza.moaai.domain.ai.dto.PromptRefineResponse;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromptRefineService {
  private static final String SYSTEM_PROMPT = """
      당신은 세계 최고 수준의 프롬프트 엔지니어입니다.
      사용자가 입력한 원본 프롬프트를 분석하여, AI가 훨씬 정확하고 완성도 높은 답변을 생성할 수 있도록 구조화하고 개선해 주세요.

      개선 원칙:
      1. 역할(Persona), 맥락(Context), 구체적 지시사항(Task), 제약 조건(Constraints), 출력 형식(Output Format)을 명확히 반영합니다.
      2. 원본 프롬프트의 핵심 의도를 유지하면서 모호한 표현을 구체화합니다.
      3. 최종 개선 프롬프트(refinedPrompt)는 사용자가 복사해서 바로 사용할 수 있는 자연스러운 한국어로 작성합니다.
      4. 어떤 부분을 어떻게 개선했는지 핵심 포인트 3~5가지를 improvements 배열에 간결하게 정리합니다.
      """;

  private final ChatClient chatClient;
  private final AiRateLimitService aiRateLimitService;

  public PromptRefineService(ChatClient.Builder chatClientBuilder, AiRateLimitService aiRateLimitService) {
    this.chatClient = chatClientBuilder.build();
    this.aiRateLimitService = aiRateLimitService;
  }

  public PromptRefineResponse refine(Long userId, PromptRefineRequest req) {
    long remaining = aiRateLimitService.checkAndIncrement(userId);

    try {
      AiRefineOutput output = chatClient.prompt()
          .system(SYSTEM_PROMPT)
          .user(buildUserPrompt(req))
          .call()
          .entity(AiRefineOutput.class);

      if (output == null || output.refinedPrompt() == null || output.refinedPrompt().isBlank())
        throw new IllegalStateException("Gemini 응답이 비어 있습니다.");

      List<String> improvements = (output.improvements() != null)
          ? output.improvements()
          : Collections.emptyList();

      return new PromptRefineResponse(
          req.originalPrompt(),
          output.refinedPrompt(),
          improvements,
          remaining);
    } catch (Exception e) {
      aiRateLimitService.rollback(userId);

      log.error("Gemini 프롬프트 개선 호출 실패 - userId={}", userId, e);

      throw new BusinessException(ErrorCode.AI_API_ERROR);
    }
  }

  public long getRemainingCount(Long userId) {
    return aiRateLimitService.getRemainingCount(userId);
  }

  private String buildUserPrompt(PromptRefineRequest req) {
    StringBuilder sb = new StringBuilder();
    sb.append("[원본 프롬프트]\n").append(req.originalPrompt());

    if (req.targetModel() != null)
      sb.append("\n\n[대상 AI 모델]\n").append(req.targetModel().name());

    if (req.goal() != null && !req.goal().isBlank())
      sb.append("\n\n[추가 개선 목표]\n").append(req.goal());

    return sb.toString();
  }

  public record AiRefineOutput(
      String refinedPrompt,
      List<String> improvements) {
  }
}
