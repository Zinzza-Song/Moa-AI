package com.songzinzza.moaai.domain.news.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.songzinzza.moaai.domain.news.dto.AiNewsCreateRequest;
import com.songzinzza.moaai.domain.news.repository.AiNewsRepository;
import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AiNewsServiceTest {
  @Mock
  private AiNewsRepository aiNewsRepository;

  @InjectMocks
  private AiNewsService aiNewsService;

  @Test
  @DisplayName("이미 등록된 originalUrl로 뉴스 생성 시 DUPLICATE_NEWS_URL 예외가 발생")
  void create_duplicateUrl_throwsException() {
    AiNewsCreateRequest req = mock(AiNewsCreateRequest.class);
    given(req.originalUrl()).willReturn("https://example.com/ai-news-1");
    given(aiNewsRepository.existsByOriginalUrl("https://example.com/ai-news-1")).willReturn(true);

    assertThatThrownBy(() -> aiNewsService.create(req))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.DUPLICATE_NEWS_URL);

    verify(aiNewsRepository, never()).save(any());
  }

  @Test
  @DisplayName("존재하지 않는 뉴스 삭제 시 NEWS_NOT_FOUND 예외가 발생")
  void delete_notFound_throwsException() {
    given(aiNewsRepository.findById(999L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> aiNewsService.delete(999L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.NEWS_NOT_FOUND);

    verify(aiNewsRepository, never()).delete(any());
  }
}
