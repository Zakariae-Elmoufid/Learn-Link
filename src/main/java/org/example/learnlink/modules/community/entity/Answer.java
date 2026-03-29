package org.example.learnlink.modules.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an answer to a community question
 */
@Entity
@Table(name = "community_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "vote_count", nullable = false)
    @Builder.Default
    private Long voteCount = 0L;

    @Column(name = "upvote_count", nullable = false)
    @Builder.Default
    private Long upvoteCount = 0L;

    @Column(name = "downvote_count", nullable = false)
    @Builder.Default
    private Long downvoteCount = 0L;

    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft delete fields for moderation
    @Column(name = "hidden", nullable = false)
    @Builder.Default
    private Boolean hidden = false;

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    @Column(name = "hidden_by")
    private Long hiddenBy;

    @Column(name = "hidden_reason")
    private String hiddenReason;

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
     * Increment upvote count
     */
    public void incrementUpvoteCount() {
        this.upvoteCount++;
        recalculateVoteCount();
    }

    /**
     * Decrement upvote count
     */
    public void decrementUpvoteCount() {
        if (this.upvoteCount > 0) {
            this.upvoteCount--;
            recalculateVoteCount();
        }
    }

    /**
     * Increment downvote count
     */
    public void incrementDownvoteCount() {
        this.downvoteCount++;
        recalculateVoteCount();
    }

    /**
     * Decrement downvote count
     */
    public void decrementDownvoteCount() {
        if (this.downvoteCount > 0) {
            this.downvoteCount--;
            recalculateVoteCount();
        }
    }

    /**
     * Recalculate total vote count
     */
    private void recalculateVoteCount() {
        this.voteCount = this.upvoteCount - this.downvoteCount;
    }

    /**
     * Mark answer as accepted
     */
    public void markAsAccepted() {
        this.isAccepted = true;
    }
}

