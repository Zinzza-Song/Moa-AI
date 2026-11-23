package com.songzinzza.moaai.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.songzinzza.moaai.global.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessExceptions(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("BusinessException: [{}] {}", errorCode.getCode(), errorCode.getMessage());

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

    return ResponseEntity
        .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unhandled Exception: ", e);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
  }
}
