package org.example.learnlink.modules.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.admin.dto.request.ModerationActionRequest;
import org.example.learnlink.modules.admin.dto.response.*;
import org.example.learnlink.modules.admin.service.AdminModerationService;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for content moderation operations.
 * Accessible by ADMIN and MODERATOR users (with permission restrictions).
 */
@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@Tag(name = "Admin Content Moderation", description = "Endpoints for moderating posts, comments, questions, and answers")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    // ==================== POST MODERATION ====================

    @GetMapping("/posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get all posts for moderation", description = "Retrieves all posts including hidden ones")
    public ResponseEntity<Page<PostModerationDto>> getAllPostsForModeration(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<PostModerationDto> posts = adminModerationService.getAllPostsForModeration(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hidden posts", description = "Retrieves only soft-deleted posts (Admin only)")
    public ResponseEntity<Page<PostModerationDto>> getHiddenPosts(
            @PageableDefault(size = 20, sort = "hiddenAt") Pageable pageable) {
        Page<PostModerationDto> posts = adminModerationService.getHiddenPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @PatchMapping("/posts/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Hide a post", description = "Soft deletes a post (makes it invisible to users)")
    public ResponseEntity<ModerationActionResponse> hidePost(
            @AuthenticationPrincipal CustomUserDetails moderator,
            @Parameter(description = "Post ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.hidePost(id, moderator.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/posts/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore a hidden post", description = "Restores a soft-deleted post (Admin only)")
    public ResponseEntity<ModerationActionResponse> restorePost(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Post ID") @PathVariable Long id,
            @Parameter(description = "Reason for restoration") @RequestParam(required = false) String reason) {
        ModerationActionResponse response = adminModerationService.restorePost(id, admin.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a post", description = "Permanently removes a post from the system (Admin only)")
    public ResponseEntity<ModerationActionResponse> permanentlyDeletePost(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Post ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.permanentlyDeletePost(id, admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== COMMENT MODERATION ====================

    @GetMapping("/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get all comments for moderation", description = "Retrieves all comments including hidden ones")
    public ResponseEntity<Page<CommentModerationDto>> getAllCommentsForModeration(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CommentModerationDto> comments = adminModerationService.getAllCommentsForModeration(pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comments/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hidden comments", description = "Retrieves only soft-deleted comments (Admin only)")
    public ResponseEntity<Page<CommentModerationDto>> getHiddenComments(
            @PageableDefault(size = 20, sort = "hiddenAt") Pageable pageable) {
        Page<CommentModerationDto> comments = adminModerationService.getHiddenComments(pageable);
        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/comments/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Hide a comment", description = "Soft deletes a comment")
    public ResponseEntity<ModerationActionResponse> hideComment(
            @AuthenticationPrincipal CustomUserDetails moderator,
            @Parameter(description = "Comment ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.hideComment(id, moderator.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/comments/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore a hidden comment", description = "Restores a soft-deleted comment (Admin only)")
    public ResponseEntity<ModerationActionResponse> restoreComment(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Comment ID") @PathVariable Long id,
            @Parameter(description = "Reason for restoration") @RequestParam(required = false) String reason) {
        ModerationActionResponse response = adminModerationService.restoreComment(id, admin.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a comment", description = "Permanently removes a comment (Admin only)")
    public ResponseEntity<ModerationActionResponse> permanentlyDeleteComment(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Comment ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.permanentlyDeleteComment(id, admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== QUESTION MODERATION ====================

    @GetMapping("/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get all questions for moderation", description = "Retrieves all questions including hidden ones")
    public ResponseEntity<Page<QuestionModerationDto>> getAllQuestionsForModeration(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<QuestionModerationDto> questions = adminModerationService.getAllQuestionsForModeration(pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hidden questions", description = "Retrieves only soft-deleted questions (Admin only)")
    public ResponseEntity<Page<QuestionModerationDto>> getHiddenQuestions(
            @PageableDefault(size = 20, sort = "hiddenAt") Pageable pageable) {
        Page<QuestionModerationDto> questions = adminModerationService.getHiddenQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    @PatchMapping("/questions/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Hide a question", description = "Soft deletes a question")
    public ResponseEntity<ModerationActionResponse> hideQuestion(
            @AuthenticationPrincipal CustomUserDetails moderator,
            @Parameter(description = "Question ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.hideQuestion(id, moderator.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/questions/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore a hidden question", description = "Restores a soft-deleted question (Admin only)")
    public ResponseEntity<ModerationActionResponse> restoreQuestion(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Question ID") @PathVariable Long id,
            @Parameter(description = "Reason for restoration") @RequestParam(required = false) String reason) {
        ModerationActionResponse response = adminModerationService.restoreQuestion(id, admin.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a question", description = "Permanently removes a question (Admin only)")
    public ResponseEntity<ModerationActionResponse> permanentlyDeleteQuestion(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Question ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.permanentlyDeleteQuestion(id, admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== ANSWER MODERATION ====================

    @GetMapping("/answers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get all answers for moderation", description = "Retrieves all answers including hidden ones")
    public ResponseEntity<Page<AnswerModerationDto>> getAllAnswersForModeration(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AnswerModerationDto> answers = adminModerationService.getAllAnswersForModeration(pageable);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/answers/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hidden answers", description = "Retrieves only soft-deleted answers (Admin only)")
    public ResponseEntity<Page<AnswerModerationDto>> getHiddenAnswers(
            @PageableDefault(size = 20, sort = "hiddenAt") Pageable pageable) {
        Page<AnswerModerationDto> answers = adminModerationService.getHiddenAnswers(pageable);
        return ResponseEntity.ok(answers);
    }

    @PatchMapping("/answers/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Hide an answer", description = "Soft deletes an answer")
    public ResponseEntity<ModerationActionResponse> hideAnswer(
            @AuthenticationPrincipal CustomUserDetails moderator,
            @Parameter(description = "Answer ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.hideAnswer(id, moderator.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/answers/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore a hidden answer", description = "Restores a soft-deleted answer (Admin only)")
    public ResponseEntity<ModerationActionResponse> restoreAnswer(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Answer ID") @PathVariable Long id,
            @Parameter(description = "Reason for restoration") @RequestParam(required = false) String reason) {
        ModerationActionResponse response = adminModerationService.restoreAnswer(id, admin.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/answers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete an answer", description = "Permanently removes an answer (Admin only)")
    public ResponseEntity<ModerationActionResponse> permanentlyDeleteAnswer(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "Answer ID") @PathVariable Long id,
            @Valid @RequestBody ModerationActionRequest request) {
        ModerationActionResponse response = adminModerationService.permanentlyDeleteAnswer(id, admin.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ==================== MODERATION LOGS ====================

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get moderation logs", description = "Retrieves all moderation action logs (Admin only)")
    public ResponseEntity<Page<ModerationLogDto>> getModerationLogs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ModerationLogDto> logs = adminModerationService.getModerationLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/moderator/{moderatorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get logs by moderator", description = "Retrieves moderation logs for a specific moderator (Admin only)")
    public ResponseEntity<Page<ModerationLogDto>> getModerationLogsByModerator(
            @Parameter(description = "Moderator User ID") @PathVariable Long moderatorId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ModerationLogDto> logs = adminModerationService.getModerationLogsByModerator(moderatorId, pageable);
        return ResponseEntity.ok(logs);
    }
}
