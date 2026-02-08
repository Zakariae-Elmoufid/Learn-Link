package org.example.learnlink.modules.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for comment response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long answerId;
    private Long userId;
    private String content;
    private Long likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

