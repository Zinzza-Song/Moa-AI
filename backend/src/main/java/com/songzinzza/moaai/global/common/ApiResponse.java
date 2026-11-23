package com.songzinzza.moaai.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> { // 프론트엔드 연동을 위한 표준 응답 포멧
  private final boolean success;
  private final String code;
  private final String message;
  private final T data;

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "SUCESS", "요청이 성공적으로 처리되었습니다.", data);
  }

  public static ApiResponse<Void> ok() {
    return new ApiResponse<Void>(true, "SUCESS", "요청이 성공적으로 처리되었습니다.", null);
  }

  public static ApiResponse<Void> fail(String code, String message) {
    return new ApiResponse<Void>(false, code, message, null);
  }
}
