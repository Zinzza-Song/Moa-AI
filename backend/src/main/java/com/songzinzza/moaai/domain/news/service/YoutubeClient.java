package com.songzinzza.moaai.domain.news.service;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.songzinzza.moaai.domain.news.config.YoutubeProperties;
import com.songzinzza.moaai.domain.news.entity.AiVideo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeClient {
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final YoutubeProperties youtubeProperties;

  public List<AiVideo> fetchAiVideos() {
    if (!youtubeProperties.isEnabled() || youtubeProperties.getApiKey() == null
        || youtubeProperties.getApiKey().isBlank()) {
      log.warn("YouTube 수집이 비활성화되었거나 API 키가 설정되지 않았습니다.");

      return List.of();
    }

    List<AiVideo> videos = new ArrayList<>();

    try {
      URI uri = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
          .queryParam("key", youtubeProperties.getApiKey())
          .queryParam("part", "snippet")
          .queryParam("q", youtubeProperties.getKeyword())
          .queryParam("type", "video")
          .queryParam("maxResults", youtubeProperties.getMaxResults())
          .queryParam("order", "date")
          .build()
          .encode()
          .toUri();

      String response = restTemplate.getForObject(uri, String.class);
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.get("items");

      if (items != null && items.isArray()) {
        for (JsonNode item : items) {
          String videoId = item.path("id").path("videoId").asText();
          
          if (videoId.isEmpty())
            continue;

          JsonNode snippet = item.path("snippet");
          String title = snippet.path("title").asText();
          String channelTitle = snippet.path("channelTitle").asText();
          String thumbnailUrl = snippet.path("thumbnails").path("medium").path("url").asText();
          String publishedAtStr = snippet.path("publishedAt").asText();

          LocalDateTime publishedAt = LocalDateTime.ofInstant(
              Instant.parse(publishedAtStr), ZoneId.systemDefault());

          videos.add(AiVideo.builder()
              .videoId(videoId)
              .title(title)
              .channelTitle(channelTitle)
              .thumbnailUrl(thumbnailUrl)
              .publishedAt(publishedAt)
              .build());
        }
      }
    } catch (Exception e) {
      log.error("YouTube API 호출 실패 - keyword={}", youtubeProperties.getKeyword(), e);
    }

    return videos;
  }
}
