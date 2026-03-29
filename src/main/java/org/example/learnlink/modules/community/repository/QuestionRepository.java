package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Question entity
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions by user ID
     */
    Page<Question> findByUserIdAndHiddenIsFalse(Long userId, Pageable pageable);

    /**
     * Find all unresolved questions
     */
    Page<Question> findByIsResolvedFalseAndHiddenIsFalse(Pageable pageable);

    /**
     * Find all resolved questions
     */
    Page<Question> findByIsResolvedTrueAndHiddenIsFalse(Pageable pageable);

    /**
     * Search questions by keyword in title or content
     */
    @Query("SELECT q FROM Question q WHERE q.hidden = false AND (LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY q.createdAt DESC")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count questions by user
     */
    long countByUserId(Long userId);

    /**
     * Find most viewed questions
     */
    @Query("SELECT q FROM Question q WHERE q.hidden = false ORDER BY q.viewCount DESC")
    Page<Question> findMostViewedQuestions(Pageable pageable);

    // Moderation queries
    
    /**
     * Find all hidden questions
     */
    Page<Question> findByHiddenTrue(Pageable pageable);

    /**
     * Find all visible (not hidden) questions
     */
    Page<Question> findByHiddenFalse(Pageable pageable);

    /**
     * Find recent questions by user ordered by creation date
     */
    List<Question> findByUserIdAndHiddenIsFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count resolved questions by user
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.userId = :userId AND q.isResolved = true")
    long countResolvedQuestionsByUserId(@Param("userId") Long userId);
}

