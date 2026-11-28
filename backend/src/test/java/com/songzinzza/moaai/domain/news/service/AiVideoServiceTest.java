package com.songzinzza.moaai.domain.news.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.songzinzza.moaai.domain.news.dto.AiVideoResponse;
import com.songzinzza.moaai.domain.news.entity.AiVideo;
import com.songzinzza.moaai.domain.news.repository.AiVideoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AiVideoServiceTest {
  @Mock
  private AiVideoRepository aiVideoRepository;

  @InjectMocks
  private AiVideoService aiVideoService;

  @Test
  @DisplayName("최신 AI 유튜브 영상 조회 시 리스트를 반환하고 URL이 알맞게 포맷팅")
  void getRecentVideos_success() {
    // given
    AiVideo video = AiVideo.builder()
        .videoId("testVideo123")
        .title("인공지능 대격변 뉴스")
        .channelTitle("AI 스포트라이트")
        .thumbnailUrl("https://img.youtube.com/test.jpg")
        .publishedAt(LocalDateTime.now())
        .build();

    given(aiVideoRepository.findRecentVideos(any(Pageable.class)))
        .willReturn(List.of(video));

    // when
    List<AiVideoResponse> response = aiVideoService.getRecentVideos(12).getVideos();

    // then
    assertThat(response).hasSize(1);
    assertThat(response.get(0).videoId()).isEqualTo("testVideo123");
    assertThat(response.get(0).videoUrl()).isEqualTo("https://www.youtube.com/watch?v=testVideo123");
  }
}
