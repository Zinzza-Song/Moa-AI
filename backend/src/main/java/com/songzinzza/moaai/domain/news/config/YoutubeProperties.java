package com.songzinzza.moaai.domain.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.youtube")
public class YoutubeProperties {
  private boolean enabled = true;
  private String apiKey;
  private String keyword = "AI 뉴스 AI 트렌드";
  private int maxResults = 15;
}
