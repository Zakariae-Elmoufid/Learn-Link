package org.example.learnlink.modules.matching.service;

import org.example.learnlink.modules.matching.dto.response.MatchSuggestionResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for the matching algorithm.
 * Provides study partner suggestions based on compatibility scoring.
 */
public interface IMatchingService {

    /**
     * Get match suggestions for a user.
     * Returns users sorted by compatibility score, excluding already connected users.
     *
     * @param userId the ID of the user requesting suggestions
     * @param limit  maximum number of suggestions to return (default: 10)
     * @return list of match suggestions sorted by compatibility score (descending)
     */
    List<MatchSuggestionResponse> getSuggestions(Long userId, int limit);

    /**
     * Get match suggestions with default limit of 10.
     *
     * @param userId the ID of the user requesting suggestions
     * @return list of match suggestions
     */
    default List<MatchSuggestionResponse> getSuggestions(Long userId) {
        return getSuggestions(userId, 10);
    }

    /**
     * Calculate compatibility score between two users.
     * Score is based on:
     * - 40% Common subjects
     * - 30% Availability overlap (future implementation)
     * - 20% Academic level proximity
     * - 10% Learning style match (future implementation)
     *
     * @param user1Id first user ID
     * @param user2Id second user ID
     * @return compatibility score between 0 and 100
     */
    BigDecimal calculateCompatibility(Long user1Id, Long user2Id);

    /**
     * Get filtered suggestions by subject.
     *
     * @param userId    the ID of the user requesting suggestions
     * @param subjectId the subject ID to filter by
     * @param limit     maximum number of suggestions
     * @return filtered list of match suggestions
     */
    List<MatchSuggestionResponse> getSuggestionsBySubject(Long userId, Long subjectId, int limit);
}
