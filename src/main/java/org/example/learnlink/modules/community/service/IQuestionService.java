package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AskQuestionRequest;
import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for question service
 */
public interface IQuestionService {

    /**
     * Ask a new question
     */
    QuestionResponse askQuestion(Long userId, AskQuestionRequest request);

    /**
     * Get question by ID
     */
    QuestionResponse getQuestionById(Long questionId);

    /**
     * Get all questions for a user
     */
    Page<QuestionResponse> getUserQuestions(Long userId, Pageable pageable);

    /**
     * Get unresolved questions
     */
    Page<QuestionResponse> getUnresolvedQuestions(Pageable pageable);

    /**
     * Get resolved questions
     */
    Page<QuestionResponse> getResolvedQuestions(Pageable pageable);

    /**
     * Search questions
     */
    Page<QuestionResponse> searchQuestions(String keyword, Pageable pageable);

    /**
     * Get all questions
     */
    Page<QuestionResponse> getAllQuestions(Pageable pageable);

    /**
     * Update a question
     */
    QuestionResponse updateQuestion(Long questionId, Long userId, AskQuestionRequest request);

    /**
     * Delete a question
     */
    void deleteQuestion(Long questionId, Long userId);

    /**
     * Get most viewed questions
     */
    Page<QuestionResponse> getMostViewedQuestions(Pageable pageable);
}

