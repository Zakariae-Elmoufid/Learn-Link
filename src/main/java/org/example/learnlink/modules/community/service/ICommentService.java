package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AddCommentRequest;
import org.example.learnlink.modules.community.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface for comment service
 */
public interface ICommentService {

    /**
     * Add comment to a post
     */
    CommentResponse addCommentToPost(Long postId, Long userId, AddCommentRequest request);

    /**
     * Add comment to an answer
     */
    CommentResponse addCommentToAnswer(Long answerId, Long userId, AddCommentRequest request);

    /**
     * Get comment by ID
     */
    CommentResponse getCommentById(Long commentId);

    /**
     * Get all comments for a post
     */
    List<CommentResponse> getCommentsForPost(Long postId);

    /**
     * Get all comments for an answer
     */
    List<CommentResponse> getCommentsForAnswer(Long answerId);

    /**
     * Get all comments by user
     */
    Page<CommentResponse> getUserComments(Long userId, Pageable pageable);

    /**
     * Update a comment
     */
    CommentResponse updateComment(Long commentId, Long userId, AddCommentRequest request);

    /**
     * Delete a comment
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * Like a comment
     */
    void likeComment(Long commentId, Long userId);

    /**
     * Unlike a comment
     */
    void unlikeComment(Long commentId, Long userId);
}

