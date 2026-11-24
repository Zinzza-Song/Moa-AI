package com.songzinzza.moaai.domain.prompt.dto;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromptUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.") @Size(max = 150, message = "제목은 150자 이하로 입력해 주세요.") String title,

    @NotBlank(message = "프롬프트 내용은 필수입니다.") String content,

    String resultExample,

    @NotNull(message = "카테고리는 필수입니다.") PromptCategory category,

    @NotNull(message = "AI 모델은 필수입니다.") AiModel aiModel) {
}
