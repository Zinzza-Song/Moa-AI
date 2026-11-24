package com.songzinzza.moaai.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode { // 에러 코드들
  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

  // Auth & User
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않거나 만료된 토큰입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

  // Prompt
  PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "프롬프트를 찾을 수 없습니다."),
  NOT_PROMPT_OWNER(HttpStatus.FORBIDDEN, "P002", "해당 프롬프트에 대한 수정 / 삭제 권한이 없습니다."),
  AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "P003", "일일 AI 프롬프트 개선 호출 한도를 초과했습니다."),

  // News
  NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "AI 트렌드 뉴스를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
