package org.example.learnlink.modules.community.controller;

import org.example.learnlink.modules.community.dto.AddCommentRequest;
import org.example.learnlink.modules.community.dto.CommentResponse;
import org.example.learnlink.modules.community.service.ICommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Comment management
 */
@RestController
@RequestMapping("/api/community/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * Add comment to a post
     * POST /api/community/comments/post/{postId}
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> addCommentToPost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCommentRequest request) {
        CommentResponse response = commentService.addCommentToPost(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Add comment to an answer
     * POST /api/community/comments/answer/{answerId}
     */
    @PostMapping("/answer/{answerId}")
    public ResponseEntity<CommentResponse> addCommentToAnswer(
            @PathVariable Long answerId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCommentRequest request) {
        CommentResponse response = commentService.addCommentToAnswer(answerId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get comment by ID
     * GET /api/community/comments/{commentId}
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get comments for a post
     * GET /api/community/comments/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsForPost(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsForPost(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get comments for an answer
     * GET /api/community/comments/answer/{answerId}
     */
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<List<CommentResponse>> getCommentsForAnswer(@PathVariable Long answerId) {
        List<CommentResponse> comments = commentService.getCommentsForAnswer(answerId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Get user comments
     * GET /api/community/comments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentResponse>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getUserComments(userId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update a comment
     * PUT /api/community/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCommentRequest request) {
        CommentResponse response = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment
     * DELETE /api/community/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Like a comment
     * POST /api/community/comments/{commentId}/like
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) {
        commentService.likeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unlike a comment
     * DELETE /api/community/comments/{commentId}/like
     */
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) {
        commentService.unlikeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }
}

