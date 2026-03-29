package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AnswerResponse;
import org.example.learnlink.modules.community.dto.ProvideAnswerRequest;
import org.example.learnlink.modules.community.entity.VoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface for answer service
 */
public interface IAnswerService {

    /**
     * Provide an answer to a question
     */
    AnswerResponse provideAnswer(Long questionId, Long userId, ProvideAnswerRequest request);

    /**
     * Get answer by ID
     */
    AnswerResponse getAnswerById(Long answerId);

    /**
     * Get all answers for a question
     */
    List<AnswerResponse> getAnswersByQuestion(Long questionId);

    /**
     * Get all answers by user
     */
    Page<AnswerResponse> getUserAnswers(Long userId, Pageable pageable);

    /**
     * Get top rated answers
     */
    Page<AnswerResponse> getTopAnswers(Pageable pageable);

    /**
     * Update an answer
     */
    AnswerResponse updateAnswer(Long answerId, Long userId, ProvideAnswerRequest request);

    /**
     * Delete an answer
     */
    void deleteAnswer(Long answerId, Long userId);

    /**
     * Accept an answer as the best answer
     */
    void acceptAnswer(Long questionId, Long answerId, Long userId);

    /**
     * Vote on an answer
     */
    void voteAnswer(Long answerId, Long userId, VoteType voteType);

    /**
     * Remove vote from an answer
     */
    void removeVote(Long answerId, Long userId);

    /**
     * Check if answer exists by question and user
     */
    boolean answerExistsByQuestionAndUser(Long questionId, Long userId);
}

