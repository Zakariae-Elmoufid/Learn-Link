package org.example.learnlink.modules.community.controller;

import org.example.learnlink.modules.community.dto.AnswerResponse;
import org.example.learnlink.modules.community.dto.ProvideAnswerRequest;
import org.example.learnlink.modules.community.entity.VoteType;
import org.example.learnlink.modules.community.service.IAnswerService;
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
 * REST Controller for Answer management
 */
@RestController
@RequestMapping("/api/community/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final IAnswerService answerService;

    /**
     * Provide an answer to a question
     * POST /api/community/answers
     * Body: {questionId, content}
     */
    @PostMapping
    public ResponseEntity<AnswerResponse> provideAnswer(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long questionId,
            @Valid @RequestBody ProvideAnswerRequest request) {
        AnswerResponse response = answerService.provideAnswer(questionId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get answer by ID
     * GET /api/community/answers/{answerId}
     */
    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Long answerId) {
        AnswerResponse response = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get answers for a question
     * GET /api/community/answers/question/{questionId}
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestion(@PathVariable Long questionId) {
        List<AnswerResponse> answers = answerService.getAnswersByQuestion(questionId);
        return ResponseEntity.ok(answers);
    }

    /**
     * Get user answers
     * GET /api/community/answers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AnswerResponse>> getUserAnswers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AnswerResponse> answers = answerService.getUserAnswers(userId, pageable);
        return ResponseEntity.ok(answers);
    }

    /**
     * Get top answers
     * GET /api/community/answers/top
     */
    @GetMapping("/top")
    public ResponseEntity<Page<AnswerResponse>> getTopAnswers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AnswerResponse> answers = answerService.getTopAnswers(pageable);
        return ResponseEntity.ok(answers);
    }

    /**
     * Update an answer
     * PUT /api/community/answers/{answerId}
     */
    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> updateAnswer(
            @PathVariable Long answerId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ProvideAnswerRequest request) {
        AnswerResponse response = answerService.updateAnswer(answerId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an answer
     * DELETE /api/community/answers/{answerId}
     */
    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long answerId,
            @RequestHeader("X-User-Id") Long userId) {
        answerService.deleteAnswer(answerId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Accept an answer as the best answer
     * POST /api/community/answers/{answerId}/accept
     * Params: questionId
     */
    @PostMapping("/{answerId}/accept")
    public ResponseEntity<Void> acceptAnswer(
            @PathVariable Long answerId,
            @RequestParam Long questionId,
            @RequestHeader("X-User-Id") Long userId) {
        answerService.acceptAnswer(questionId, answerId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Vote on an answer
     * POST /api/community/answers/{answerId}/vote
     * Body: {voteType: UPVOTE|DOWNVOTE}
     */
    @PostMapping("/{answerId}/vote")
    public ResponseEntity<Void> voteAnswer(
            @PathVariable Long answerId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam VoteType voteType) {
        answerService.voteAnswer(answerId, userId, voteType);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove vote from an answer
     * DELETE /api/community/answers/{answerId}/vote
     */
    @DeleteMapping("/{answerId}/vote")
    public ResponseEntity<Void> removeVote(
            @PathVariable Long answerId,
            @RequestHeader("X-User-Id") Long userId) {
        answerService.removeVote(answerId, userId);
        return ResponseEntity.ok().build();
    }
}

