package com.songzinzza.moaai.domain.prompt.repository;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}