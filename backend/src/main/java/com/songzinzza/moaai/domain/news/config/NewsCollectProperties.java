package com.songzinzza.moaai.domain.news.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.news.collect")
public class NewsCollectProperties {
  private boolean enabled = true;
  private int maxPerFeed = 2;
  private List<FeedSource> feeds = new ArrayList<>();

  @Getter
  @Setter
  public static class FeedSource {
    private String name;
    private String url;
    private List<String> keywords = new ArrayList<>();
  }
}
