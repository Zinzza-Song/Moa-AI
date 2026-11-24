package com.songzinzza.moaai.domain.prompt.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.songzinzza.moaai.domain.prompt.entity.PromptBookmark;

public interface PromptBookmarkRepository extends JpaRepository<PromptBookmark, Long> {
  Optional<PromptBookmark> findByUserIdAndPromptId(Long userId, Long promptId);

  boolean existsByUserIdAndPromptId(Long userId, Long promptId);

  @Query("SELECT pb.prompt.id FROM PromptBookmark pb WHERE pb.user.id = :userId AND pb.prompt.id IN :promptIds")
  Set<Long> findBookmarkedPromptIds(@Param("userId") Long userId, @Param("promptIds") List<Long> promptIds);

  void deleteByPromptId(Long promptId);
}
