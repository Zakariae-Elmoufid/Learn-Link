package org.example.learnlink.modules.community.dto;

import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for post response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private PostType type;
    private PostCategory category;
    private Long viewCount;
    private Long likesCount;
    private Long commentsCount;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
    private Boolean likedByCurrentUser;
}

