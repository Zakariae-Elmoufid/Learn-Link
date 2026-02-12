package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.AnswerVote;
import org.example.learnlink.modules.community.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AnswerVote entity
 */
@Repository
public interface AnswerVoteRepository extends JpaRepository<AnswerVote, Long> {

    /**
     * Find a vote by answer ID and user ID
     */
    Optional<AnswerVote> findByAnswerIdAndUserId(Long answerId, Long userId);

    /**
     * Check if a user has voted on an answer
     */
    boolean existsByAnswerIdAndUserId(Long answerId, Long userId);

    /**
     * Count votes on an answer
     */
    long countByAnswerId(Long answerId);

    /**
     * Delete a vote
     */
    void deleteByAnswerIdAndUserId(Long answerId, Long userId);

    /**
     * Count upvotes on an answer
     */
    long countByAnswerIdAndVoteType(Long answerId, VoteType voteType);
}

