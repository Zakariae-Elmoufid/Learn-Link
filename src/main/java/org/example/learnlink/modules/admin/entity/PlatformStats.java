package org.example.learnlink.modules.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to store aggregated platform statistics.
 * Updated via event listeners to avoid direct coupling with other modules.
 * Uses a single row pattern with id=1 for the main statistics record.
 */
@Entity
@Table(name = "platform_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== User Statistics ====================
    
    @Column(name = "total_users", nullable = false)
    @Builder.Default
    private Long totalUsers = 0L;

    @Column(name = "active_users_last_7_days", nullable = false)
    @Builder.Default
    private Long activeUsersLast7Days = 0L;

    @Column(name = "active_users_last_30_days", nullable = false)
    @Builder.Default
    private Long activeUsersLast30Days = 0L;

    @Column(name = "new_users_this_week", nullable = false)
    @Builder.Default
    private Long newUsersThisWeek = 0L;

    @Column(name = "new_users_this_month", nullable = false)
    @Builder.Default
    private Long newUsersThisMonth = 0L;

    // ==================== Content Statistics ====================
    
    @Column(name = "total_posts", nullable = false)
    @Builder.Default
    private Long totalPosts = 0L;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Long totalQuestions = 0L;

    @Column(name = "total_answers", nullable = false)
    @Builder.Default
    private Long totalAnswers = 0L;

    @Column(name = "total_comments", nullable = false)
    @Builder.Default
    private Long totalComments = 0L;

    @Column(name = "posts_this_week", nullable = false)
    @Builder.Default
    private Long postsThisWeek = 0L;

    // ==================== Task Statistics ====================
    
    @Column(name = "total_tasks", nullable = false)
    @Builder.Default
    private Long totalTasks = 0L;

    @Column(name = "completed_tasks", nullable = false)
    @Builder.Default
    private Long completedTasks = 0L;

    // ==================== Engagement Statistics ====================
    
    @Column(name = "total_connections", nullable = false)
    @Builder.Default
    private Long totalConnections = 0L;

    // ==================== Gamification Statistics ====================
    
    @Column(name = "total_points_awarded", nullable = false)
    @Builder.Default
    private Long totalPointsAwarded = 0L;

    @Column(name = "total_badges_earned", nullable = false)
    @Builder.Default
    private Long totalBadgesEarned = 0L;

    // ==================== Metadata ====================
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "week_reset_at")
    private LocalDateTime weekResetAt;

    @Column(name = "month_reset_at")
    private LocalDateTime monthResetAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    // ==================== Increment Methods ====================

    public void incrementTotalUsers() {
        this.totalUsers++;
        this.newUsersThisWeek++;
        this.newUsersThisMonth++;
    }

    public void incrementTotalPosts() {
        this.totalPosts++;
        this.postsThisWeek++;
    }

    public void incrementTotalQuestions() {
        this.totalQuestions++;
    }

    public void incrementTotalAnswers() {
        this.totalAnswers++;
    }

    public void incrementTotalComments() {
        this.totalComments++;
    }

    public void incrementTotalTasks() {
        this.totalTasks++;
    }

    public void incrementCompletedTasks() {
        this.completedTasks++;
    }

    public void incrementTotalConnections() {
        this.totalConnections++;
    }

    public void addPoints(long points) {
        this.totalPointsAwarded += points;
    }

    public void incrementBadgesEarned() {
        this.totalBadgesEarned++;
    }

    public void updateActiveUsers(long last7Days, long last30Days) {
        this.activeUsersLast7Days = last7Days;
        this.activeUsersLast30Days = last30Days;
    }

    // ==================== Weekly/Monthly Reset Methods ====================

    public void resetWeeklyCounters() {
        this.newUsersThisWeek = 0L;
        this.postsThisWeek = 0L;
        this.weekResetAt = LocalDateTime.now();
    }

    public void resetMonthlyCounters() {
        this.newUsersThisMonth = 0L;
        this.monthResetAt = LocalDateTime.now();
    }
}
