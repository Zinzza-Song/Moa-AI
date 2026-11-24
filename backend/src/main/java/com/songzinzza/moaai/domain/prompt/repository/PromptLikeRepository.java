package com.songzinzza.moaai.domain.prompt.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.songzinzza.moaai.domain.prompt.entity.PromptLike;

public interface PromptLikeRepository extends JpaRepository<PromptLike, Long> {
  Optional<PromptLike> findByUserIdAndPromptId(Long userId, Long promptId);

  boolean existsByUserIdAndPromptId(Long userId, Long promptId);

  @Query("SELECT pl.prompt.id FROM PromptLike pl WHERE pl.user.id = :userId AND pl.prompt.id IN :promptIds")
  Set<Long> findLikedPromptIds(@Param("userId") Long userId, @Param("promptIds") List<Long> promptIds);

  void deleteByPromptId(Long promptId);
}
