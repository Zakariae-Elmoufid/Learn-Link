package org.example.learnlink.modules.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for answer response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    private Long id;
    private Long questionId;
    private Long userId;
    private String username;
    private String profilePictureUrl;
    private String content;
    private Long voteCount;
    private Long upvoteCount;
    private Long downvoteCount;
    private Boolean isAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean votedByCurrentUser; // UPVOTE, DOWNVOTE, or null
}

