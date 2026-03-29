package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for question moderation view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionModerationDto {

    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private Long viewCount;
    private Boolean isResolved;
    private Long acceptedAnswerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Moderation fields
    private Boolean hidden;
    private LocalDateTime hiddenAt;
    private Long hiddenBy;
    private String hiddenByUsername;
    private String hiddenReason;
}
