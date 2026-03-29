package org.example.learnlink.modules.matching.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for connection request responses
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionRequestResponse {

    private Long id;

    /**
     * Information about the sender
     */
    private Long senderId;
    private String senderFirstName;
    private String senderLastName;
    private String senderProfilePictureUrl;

    /**
     * Information about the receiver
     */
    private Long receiverId;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverProfilePictureUrl;

    /**
     * The personal message included with the request
     */
    private String message;

    /**
     * Current status of the request
     */
    private String status;

    /**
     * Compatibility score between the two users (0-100)
     */
    private BigDecimal compatibilityScore;

    /**
     * When the request was created
     */
    private LocalDateTime createdAt;

    /**
     * When the request was last updated
     */
    private LocalDateTime updatedAt;
}
