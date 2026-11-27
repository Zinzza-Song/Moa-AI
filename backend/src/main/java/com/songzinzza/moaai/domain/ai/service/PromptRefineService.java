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
  private static final int MAX_TITLE_LENGTH = 150;

  private static final String SYSTEM_PROMPT = """
      당신은 세계 최고 수준의 프롬프트 엔지니어입니다.
      사용자가 입력한 원본 프롬프트를 분석하여, AI가 훨씬 정확하고 완성도 높은 답변을 생성할 수 있도록 구조화하고 개선해 주세요.

      작성 규칙:
      1. suggestedTitle: 개선된 프롬프트의 용도와 핵심을 한눈에 파악할 수 있는 간결한 한국어 제목 (40자 이내, 따옴표 없이 작성)
      2. refinedPrompt: 역할(Persona), 맥락(Context), 구체적 지시사항(Task), 제약 조건(Constraints), 출력 형식(Output Format)을 명확히 반영해 사용자가 복사해서 바로 쓸 수 있는 자연스러운 한국어 프롬프트로 작성
      3. resultExample: 위 refinedPrompt를 실제로 AI에 입력했을 때 기대되는 구체적인 출력 예시를 한국어로 작성 (너무 길지 않게 핵심 구조가 드러나도록 5~10줄 내외)
      4. improvements: 어떤 부분을 어떻게 개선했는지 핵심 포인트 3~5가지를 배열에 간결하게 정리
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

      String suggestedTitle = truncate(output.suggestedTitle(), MAX_TITLE_LENGTH);

      String resultExample = (output.resultExample() != null ? output.resultExample().trim() : "");

      List<String> improvements = (output.improvements() != null)
          ? output.improvements()
          : Collections.emptyList();

      return new PromptRefineResponse(
          req.originalPrompt(),
          suggestedTitle,
          output.refinedPrompt(),
          resultExample,
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

  private String truncate(String value, int maxLength) {
    if (value == null)
      return "";

    String trimmed = value.trim();

    return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
  }

  public record AiRefineOutput(
      String suggestedTitle,
      String refinedPrompt,
      String resultExample,
      List<String> improvements) {
  }
}
