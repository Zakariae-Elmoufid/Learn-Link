package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;

import java.time.LocalDateTime;

/**
 * DTO for post moderation view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostModerationDto {

    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private PostType type;
    private PostCategory category;
    private Long viewCount;
    private Long likesCount;
    private Long commentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Moderation fields
    private Boolean hidden;
    private LocalDateTime hiddenAt;
    private Long hiddenBy;
    private String hiddenByUsername;
    private String hiddenReason;
}
