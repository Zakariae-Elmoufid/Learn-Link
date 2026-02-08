package org.example.learnlink.modules.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a community post (summary, tutorial, discussion)
 */
@Entity
@Table(name = "community_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PostType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PostCategory category;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Long likesCount = 0L;

    @Column(name = "comments_count", nullable = false)
    @Builder.Default
    private Long commentsCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increment likes count
     */
    public void incrementLikesCount() {
        this.likesCount++;
    }

    /**
     * Decrement likes count
     */
    public void decrementLikesCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    /**
     * Increment comments count
     */
    public void incrementCommentsCount() {
        this.commentsCount++;
    }

    /**
     * Decrement comments count
     */
    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }
}

