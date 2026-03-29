package org.example.learnlink.modules.matching.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for match suggestion responses.
 * Contains user profile information and compatibility details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSuggestionResponse {

    /**
     * User ID of the suggested match
     */
    private Long userId;

    /**
     * User profile information
     */
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private String academicLevel;

    /**
     * Overall compatibility score (0-100)
     */
    private BigDecimal compatibilityScore;

    /**
     * List of subjects in common
     */
    private List<String> commonSubjects;

    /**
     * Breakdown of compatibility scores by category
     */
    private Integer subjectMatchPercentage;
    private Integer levelMatchPercentage;

    /**
     * Connection status with this user
     */
    private Boolean hasPendingRequest;
    private Boolean isConnected;
}
