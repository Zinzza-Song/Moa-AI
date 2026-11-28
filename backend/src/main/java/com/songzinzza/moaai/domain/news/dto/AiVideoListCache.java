package com.songzinzza.moaai.domain.news.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiVideoListCache {
  private List<AiVideoResponse> videos = new ArrayList<>();

  public AiVideoListCache(List<AiVideoResponse> videos) {
    this.videos = new ArrayList<>(videos);
  }
}
