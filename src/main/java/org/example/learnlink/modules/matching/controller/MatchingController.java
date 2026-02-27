package org.example.learnlink.modules.matching.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.matching.dto.response.MatchSuggestionResponse;
import org.example.learnlink.modules.matching.service.IMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for matching/study partner suggestions.
 * Provides endpoints for finding compatible study partners.
 */
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final IMatchingService matchingService;

    /**
     * Get match suggestions for the current user.
     * Returns users sorted by compatibility score.
     * GET /api/matching/suggestions?limit=10
     *
     * @param userId the ID of the authenticated user
     * @param limit  maximum number of suggestions (default: 10, max: 50)
     * @return list of match suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<MatchSuggestionResponse>> getSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = userDetails.getId();

        // Cap limit at 50
        int cappedLimit = Math.min(limit, 50);

        List<MatchSuggestionResponse> suggestions = matchingService.getSuggestions(userId, cappedLimit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get match suggestions filtered by a specific subject.
     * GET /api/matching/suggestions/subject/{subjectId}?limit=10
     *
     * @param userId    the ID of the authenticated user
     * @param subjectId the subject ID to filter by
     * @param limit     maximum number of suggestions
     * @return filtered list of match suggestions
     */
    @GetMapping("/suggestions/subject/{subjectId}")
    public ResponseEntity<List<MatchSuggestionResponse>> getSuggestionsBySubject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = userDetails.getId();

        int cappedLimit = Math.min(limit, 50);

        List<MatchSuggestionResponse> suggestions = matchingService
                .getSuggestionsBySubject(userId, subjectId, cappedLimit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Calculate compatibility score between current user and another user.
     * GET /api/matching/compatibility/{otherUserId}
     *
     * @param otherUserId the ID of the other user
     * @return compatibility score and breakdown
     */
    @GetMapping("/compatibility/{otherUserId}")
    public ResponseEntity<Map<String, Object>> getCompatibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long otherUserId) {
        Long userId = userDetails.getId();

        BigDecimal score = matchingService.calculateCompatibility(userId, otherUserId);

        return ResponseEntity.ok(Map.of(
                "userId", otherUserId,
                "compatibilityScore", score,
                "message", getCompatibilityMessage(score)
        ));
    }

    // ==================== Private Methods ====================

    /**
     * Get a human-readable message for compatibility score.
     */
    private String getCompatibilityMessage(BigDecimal score) {
        int scoreInt = score.intValue();

        if (scoreInt >= 80) {
            return "Excellent match! You have a lot in common.";
        } else if (scoreInt >= 60) {
            return "Good match! You share similar interests.";
        } else if (scoreInt >= 40) {
            return "Moderate match. You have some things in common.";
        } else if (scoreInt >= 20) {
            return "Low match. You have different interests.";
        } else {
            return "Minimal match. Very different profiles.";
        }
    }
}
