package org.example.learnlink.modules.matching.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for established connection responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionResponse {

    /**
     * The connection ID
     */
    private Long id;

    /**
     * Information about the connected user (the other person)
     */
    private Long connectedUserId;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private String academicLevel;

    /**
     * Compatibility score with this user (0-100)
     */
    private BigDecimal compatibilityScore;

    /**
     * Current status of the connection
     */
    private String status;

    /**
     * When the connection was established
     */
    private LocalDateTime connectedAt;
}
