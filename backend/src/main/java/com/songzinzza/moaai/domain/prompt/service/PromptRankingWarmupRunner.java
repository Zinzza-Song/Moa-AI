package com.songzinzza.moaai.domain.prompt.service;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptRankingWarmupRunner implements ApplicationRunner {
  private final PromptRepository promptRepository;
  private final PromptRankingService promptRankingService;

  @Override
  public void run(ApplicationArguments args) {
    if (!promptRankingService.isEmpty())
      return;

    List<Prompt> prompts = promptRepository.findAll();
    int synced = 0;
    for (Prompt p : prompts) {
      double score = p.getLikeCount() * PromptRankingService.LIKE_SCORE
          + p.getBookmarkCount() * PromptRankingService.BOOKMARK_SCORE
          + p.getCopyCount() * PromptRankingService.COPY_SCORE;

      if (score > 0) {
        promptRankingService.syncScore(p.getId(), score);

        synced++;
      }
    }

    log.info("Redis 인기 프롬프트 랭킹 워밍업 완료 - 동기화 건수: {}", synced);
  }
}
