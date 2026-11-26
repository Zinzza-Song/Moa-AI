package com.songzinzza.moaai.domain.news.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.songzinzza.moaai.domain.news.config.NewsCollectProperties;
import com.songzinzza.moaai.domain.news.service.AiNewsCollectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiNewsScheduler {
  private final NewsCollectProperties properties;
  private final AiNewsCollectService aiNewsCollectService;

  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
  public void collectDailyAiNews() {
    if (!properties.isEnabled())
      return;

    log.info("정기 AI 뉴스 자동 수집 스케줄러 실행");

    aiNewsCollectService.collectAll();
  }
}
