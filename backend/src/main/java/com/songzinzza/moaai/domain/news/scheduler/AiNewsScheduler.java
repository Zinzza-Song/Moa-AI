package com.songzinzza.moaai.domain.news.scheduler;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.news.config.NewsCollectProperties;
import com.songzinzza.moaai.domain.news.entity.AiVideo;
import com.songzinzza.moaai.domain.news.repository.AiVideoRepository;
import com.songzinzza.moaai.domain.news.service.AiNewsCollectService;
import com.songzinzza.moaai.domain.news.service.YoutubeClient;
import com.songzinzza.moaai.global.config.RedisCacheConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiNewsScheduler {
  private final NewsCollectProperties properties;
  private final AiNewsCollectService aiNewsCollectService;
  private final YoutubeClient youtubeClient;
  private final AiVideoRepository aiVideoRepository;

  // 매일 오전 9시에 뉴스 자동 수집
  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
  public void collectDailyAiNews() {
    if (!properties.isEnabled())
      return;

    log.info("정기 AI 뉴스 자동 수집 스케줄러 실행");
    aiNewsCollectService.collectAll();
  }

  // 12시간마다 최신 AI 유튜브 영상 수집
  @Scheduled(cron = "0 0 */12 * * *", zone = "Asia/Seoul")
  @Transactional
  @CacheEvict(value = RedisCacheConfig.AI_VIDEOS, allEntries = true)
  public void collectAiVideos() {
    log.info("유튜브 AI 최신 영상 수집 시작...");

    List<AiVideo> fetched = youtubeClient.fetchAiVideos();
    int count = 0;

    for (AiVideo video : fetched) {
      if (aiVideoRepository.findByVideoId(video.getVideoId()).isEmpty()) {
        aiVideoRepository.save(video);
        count++;
      }
    }

    log.info("유튜브 AI 최신 영상 수집 완료 - 신규 등록: {}건", count);
  }
}
