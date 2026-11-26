package com.songzinzza.moaai.global.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last) {
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }

  public <R> PageResponse<R> map(Function<T, R> mapper) {
    List<R> mappedContent = this.content.stream().map(mapper).toList();

    return new PageResponse<>(
        mappedContent,
        this.page,
        this.size,
        this.totalElements,
        this.totalPages,
        this.last); // 기존 PageResponse의 나머지 필드들을 순서대로 그대로 전달
  }
}
