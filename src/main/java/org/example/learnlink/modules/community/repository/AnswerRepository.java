package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Answer entity
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Find all answers for a question
     */
    @Query("SELECT a FROM Answer a WHERE a.questionId = :questionId ORDER BY a.voteCount DESC")
    List<Answer> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * Find all answers by user
     */
    Page<Answer> findByUserId(Long userId, Pageable pageable);

    /**
     * Find the accepted answer for a question
     */
    Optional<Answer> findByQuestionIdAndIsAcceptedTrue(Long questionId);

    /**
     * Count answers for a question
     */
    long countByQuestionId(Long questionId);

    /**
     * Find top answers ordered by vote count
     */
    @Query("SELECT a FROM Answer a ORDER BY a.voteCount DESC")
    Page<Answer> findTopAnswers(Pageable pageable);

    /**
     * Check if answer exists for question by user
     */
    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);

    // Moderation queries
    
    /**
     * Find all hidden answers
     */
    Page<Answer> findByHiddenTrue(Pageable pageable);

    /**
     * Find all visible (not hidden) answers
     */
    Page<Answer> findByHiddenFalse(Pageable pageable);
}

