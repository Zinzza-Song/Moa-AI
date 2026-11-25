package com.songzinzza.moaai.domain.news.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiNewsCreateRequest(
    @NotBlank(message = "제목은 필수입니다.") @Size(max = 255, message = "제목은 255자 이하로 입력해 주세요.") String title,

    @NotBlank(message = "요약 내용은 필수입니다.") String summary,

    @NotBlank(message = "원문 URL은 필수입니다.") @Size(max = 500, message = "원문 URL은 500자 이하로 입력해 주세요.") String originalUrl,

    @NotBlank(message = "출처명은 필수입니다.") @Size(max = 100, message = "출처명은 100자 이하로 입력해 주세요.") String sourceName,

    @Size(max = 255, message = "태그는 255자 이하로 입력해 주세요.") String tags,

    LocalDateTime publishedAt) {

}
