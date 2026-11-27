package com.songzinzza.moaai.domain.prompt.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptLike;

public interface PromptLikeRepository extends JpaRepository<PromptLike, Long> {
  Optional<PromptLike> findByUserIdAndPromptId(Long userId, Long promptId);

  boolean existsByUserIdAndPromptId(Long userId, Long promptId);

  @Query("SELECT pl.prompt.id FROM PromptLike pl WHERE pl.user.id = :userId AND pl.prompt.id IN :promptIds")
  Set<Long> findLikedPromptIds(@Param("userId") Long userId, @Param("promptIds") List<Long> promptIds);

  void deleteByPromptId(Long promptId);

  @Modifying
  @Query("DELETE FROM PromptLike pl WHERE pl.user.id = :userId AND pl.prompt.id = :promptId")
  int deleteByUserIdAndPromptId(@Param("userId") Long userId, @Param("promptId") Long promptId);

  @Query(value = "SELECT p FROM PromptLike pl JOIN pl.prompt p JOIN FETCH p.user WHERE pl.user.id = :userId ORDER BY pl.createdAt DESC", countQuery = "SELECT COUNT(pl) FROM PromptLike pl WHERE pl.user.id = :userId")
  Page<Prompt> findLikedPromptsByUserId(@Param("userId") Long userId, Pageable pageable);

  long countByUserId(Long userId);
}
