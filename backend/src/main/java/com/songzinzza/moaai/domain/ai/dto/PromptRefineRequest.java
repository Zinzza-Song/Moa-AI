package com.songzinzza.moaai.domain.ai.dto;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptRefineRequest(
    @NotBlank(message = "개선할 프롬프트 내용은 필수입니다.") @Size(max = 2000, message = "프롬프트는 2000자 이하로 입력해 주세요.") String originalPrompt,

    AiModel targetModel,

    @Size(max = 300, message = "개선 목표는 300자 이하로 입력해 주세요.") String goal) {

}
