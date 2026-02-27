package org.example.learnlink.modules.community.controller;

import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.community.dto.AskQuestionRequest;
import org.example.learnlink.modules.community.dto.QuestionResponse;
import org.example.learnlink.modules.community.service.IQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Question management
 */
@RestController
@RequestMapping("/api/community/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final IQuestionService questionService;

    /**
     * Ask a new question
     * POST /api/community/questions
     */
    @PostMapping
    public ResponseEntity<QuestionResponse> askQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AskQuestionRequest request) {
        Long userId = userDetails.getId();
        QuestionResponse response = questionService.askQuestion(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get question by ID
     * GET /api/community/questions/{questionId}
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long questionId) {
        QuestionResponse response = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all questions with pagination
     * GET /api/community/questions?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<QuestionResponse>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.getAllQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get unresolved questions
     * GET /api/community/questions/unresolved
     */
    @GetMapping("/unresolved")
    public ResponseEntity<Page<QuestionResponse>> getUnresolvedQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.getUnresolvedQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get resolved questions
     * GET /api/community/questions/resolved
     */
    @GetMapping("/resolved")
    public ResponseEntity<Page<QuestionResponse>> getResolvedQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.getResolvedQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get user questions
     * GET /api/community/questions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<QuestionResponse>> getUserQuestions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.getUserQuestions(userId, pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Search questions
     * GET /api/community/questions/search?keyword=...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<QuestionResponse>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.searchQuestions(keyword, pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get most viewed questions
     * GET /api/community/questions/viewed
     */
    @GetMapping("/viewed")
    public ResponseEntity<Page<QuestionResponse>> getMostViewedQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionResponse> questions = questionService.getMostViewedQuestions(pageable);
        return ResponseEntity.ok(questions);
    }

    /**
     * Update a question
     * PUT /api/community/questions/{questionId}
     */
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AskQuestionRequest request) {
        Long userId = userDetails.getId();
        QuestionResponse response = questionService.updateQuestion(questionId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a question
     * DELETE /api/community/questions/{questionId}
     */
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        questionService.deleteQuestion(questionId, userId);
        return ResponseEntity.noContent().build();
    }
}

