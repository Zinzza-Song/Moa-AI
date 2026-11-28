package com.songzinzza.moaai.domain.news.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.songzinzza.moaai.domain.news.entity.AiVideo;

public interface AiVideoRepository extends JpaRepository<AiVideo, Long> {
  Optional<AiVideo> findByVideoId(String videoId);

  @Query("SELECT v FROM AiVideo v ORDER BY v.publishedAt DESC")
  List<AiVideo> findRecentVideos(Pageable pageable);
}
