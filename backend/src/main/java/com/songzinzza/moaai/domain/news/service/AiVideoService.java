package com.songzinzza.moaai.domain.news.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.news.dto.AiVideoListCache;
import com.songzinzza.moaai.domain.news.dto.AiVideoResponse;
import com.songzinzza.moaai.domain.news.repository.AiVideoRepository;
import com.songzinzza.moaai.global.config.RedisCacheConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiVideoService {
  private final AiVideoRepository aiVideoRepository;

  @Cacheable(value = RedisCacheConfig.AI_VIDEOS, key = "#limit")
  public AiVideoListCache getRecentVideos(int limit) {
    List<AiVideoResponse> videos = aiVideoRepository.findRecentVideos(PageRequest.of(0, limit)).stream()
        .map(AiVideoResponse::from)
        .toList();

    return new AiVideoListCache(videos);
  }
}
