package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for comment moderation view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentModerationDto {

    private Long id;
    private Long postId;
    private Long answerId;
    private Long userId;
    private String username;
    private String content;
    private Long likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Moderation fields
    private Boolean hidden;
    private LocalDateTime hiddenAt;
    private Long hiddenBy;
    private String hiddenByUsername;
    private String hiddenReason;
}
