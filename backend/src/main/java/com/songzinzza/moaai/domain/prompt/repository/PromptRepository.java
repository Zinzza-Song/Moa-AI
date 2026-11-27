package com.songzinzza.moaai.domain.prompt.repository;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

  @EntityGraph(attributePaths = { "user" })
  @Query("""
      SELECT p FROM Prompt p
      WHERE (:category IS NULL OR p.category = :category)
        AND (:aiModel IS NULL OR p.aiModel = :aiModel)
        AND (:keyword IS NULL OR :keyword = ''
             OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
      """)
  Page<Prompt> searchPrompts(
      @Param("category") PromptCategory category,
      @Param("aiModel") AiModel aiModel,
      @Param("keyword") String keyword,
      Pageable pageable);

  @EntityGraph(attributePaths = { "user" })
  Optional<Prompt> findWithUserById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Prompt p WHERE p.id = :id")
  Optional<Prompt> findByIdForUpdate(@Param("id") Long id);

  // Atomic Update(동시성 제어용)
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Prompt p SET p.copyCount = p.copyCount + 1 WHERE p.id = :id")
  int incrementCopyCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Prompt p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
  int incrementLikeCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Prompt p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :id")
  int decrementLikeCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Prompt p SET p.bookmarkCount = p.bookmarkCount + 1 WHERE p.id = :id")
  int incrementBookmarkCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Prompt p SET p.bookmarkCount = CASE WHEN p.bookmarkCount > 0 THEN p.bookmarkCount - 1 ELSE 0 END WHERE p.id = :id")
  int decrementBookmarkCount(@Param("id") Long id);

  // 갱신 직후 최신 카운트 단건 조히
  @Query("SELECT p.copyCount FROM Prompt p WHERE p.id = :id")
  Optional<Long> findCopyCountById(@Param("id") Long id);

  @Query("SELECT p.likeCount FROM Prompt p WHERE p.id = :id")
  Optional<Long> findLikeCountById(@Param("id") Long id);

  @Query("SELECT p.bookmarkCount FROM Prompt p WHERE p.id = :id")
  Optional<Long> findBookmarkCountById(@Param("id") Long id);
}