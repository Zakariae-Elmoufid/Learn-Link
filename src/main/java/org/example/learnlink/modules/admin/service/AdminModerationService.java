package org.example.learnlink.modules.admin.service;

import org.example.learnlink.modules.admin.dto.request.ModerationActionRequest;
import org.example.learnlink.modules.admin.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for content moderation operations
 */
public interface AdminModerationService {

    // ==================== POST MODERATION ====================
    
    /**
     * Get all posts for moderation (including hidden)
     */
    Page<PostModerationDto> getAllPostsForModeration(Pageable pageable);

    /**
     * Get hidden posts only
     */
    Page<PostModerationDto> getHiddenPosts(Pageable pageable);

    /**
     * Hide (soft delete) a post
     */
    ModerationActionResponse hidePost(Long postId, Long moderatorId, ModerationActionRequest request);

    /**
     * Restore a hidden post
     */
    ModerationActionResponse restorePost(Long postId, Long adminId, String reason);

    /**
     * Permanently delete a post
     */
    ModerationActionResponse permanentlyDeletePost(Long postId, Long adminId, ModerationActionRequest request);

    // ==================== COMMENT MODERATION ====================

    /**
     * Get all comments for moderation
     */
    Page<CommentModerationDto> getAllCommentsForModeration(Pageable pageable);

    /**
     * Get hidden comments only
     */
    Page<CommentModerationDto> getHiddenComments(Pageable pageable);

    /**
     * Hide (soft delete) a comment
     */
    ModerationActionResponse hideComment(Long commentId, Long moderatorId, ModerationActionRequest request);

    /**
     * Restore a hidden comment
     */
    ModerationActionResponse restoreComment(Long commentId, Long adminId, String reason);

    /**
     * Permanently delete a comment
     */
    ModerationActionResponse permanentlyDeleteComment(Long commentId, Long adminId, ModerationActionRequest request);

    // ==================== QUESTION MODERATION ====================

    /**
     * Get all questions for moderation
     */
    Page<QuestionModerationDto> getAllQuestionsForModeration(Pageable pageable);

    /**
     * Get hidden questions only
     */
    Page<QuestionModerationDto> getHiddenQuestions(Pageable pageable);

    /**
     * Hide (soft delete) a question
     */
    ModerationActionResponse hideQuestion(Long questionId, Long moderatorId, ModerationActionRequest request);

    /**
     * Restore a hidden question
     */
    ModerationActionResponse restoreQuestion(Long questionId, Long adminId, String reason);

    /**
     * Permanently delete a question
     */
    ModerationActionResponse permanentlyDeleteQuestion(Long questionId, Long adminId, ModerationActionRequest request);

    // ==================== ANSWER MODERATION ====================

    /**
     * Get all answers for moderation
     */
    Page<AnswerModerationDto> getAllAnswersForModeration(Pageable pageable);

    /**
     * Get hidden answers only
     */
    Page<AnswerModerationDto> getHiddenAnswers(Pageable pageable);

    /**
     * Hide (soft delete) an answer
     */
    ModerationActionResponse hideAnswer(Long answerId, Long moderatorId, ModerationActionRequest request);

    /**
     * Restore a hidden answer
     */
    ModerationActionResponse restoreAnswer(Long answerId, Long adminId, String reason);

    /**
     * Permanently delete an answer
     */
    ModerationActionResponse permanentlyDeleteAnswer(Long answerId, Long adminId, ModerationActionRequest request);

    // ==================== MODERATION LOGS ====================

    /**
     * Get moderation action logs
     */
    Page<ModerationLogDto> getModerationLogs(Pageable pageable);

    /**
     * Get moderation logs by moderator
     */
    Page<ModerationLogDto> getModerationLogsByModerator(Long moderatorId, Pageable pageable);
}
