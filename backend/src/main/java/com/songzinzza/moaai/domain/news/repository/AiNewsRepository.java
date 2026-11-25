package com.songzinzza.moaai.domain.news.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.songzinzza.moaai.domain.news.entity.AiNews;

public interface AiNewsRepository extends JpaRepository<AiNews, Long> {
  @Query("""
      SELECT n FROM AiNews n
      WHERE (:keyword IS NULL OR :keyword = ''
             OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(n.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:tag IS NULL OR :tag = ''
             OR LOWER(n.tags) LIKE LOWER(CONCAT('%', :tag, '%')))
      """)
  Page<AiNews> searchNews(
      @Param("keyword") String keyword,
      @Param("tag") String tag,
      Pageable pageable);

  boolean existsByOriginalUrl(String originalUrl);
}
