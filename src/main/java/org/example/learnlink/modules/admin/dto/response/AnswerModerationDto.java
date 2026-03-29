package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for answer moderation view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerModerationDto {

    private Long id;
    private Long questionId;
    private Long userId;
    private String username;
    private String content;
    private Long voteCount;
    private Long upvoteCount;
    private Long downvoteCount;
    private Boolean isAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Moderation fields
    private Boolean hidden;
    private LocalDateTime hiddenAt;
    private Long hiddenBy;
    private String hiddenByUsername;
    private String hiddenReason;
}
