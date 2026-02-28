package org.example.learnlink.modules.admin.repository;

import org.example.learnlink.modules.admin.entity.PlatformStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for platform statistics.
 * Uses single-row pattern where id=1 holds the main statistics.
 */
@Repository
public interface PlatformStatsRepository extends JpaRepository<PlatformStats, Long> {

    /**
     * Get the main statistics record (id=1)
     */
    default Optional<PlatformStats> getStats() {
        return findById(1L);
    }

    /**
     * Initialize the stats row if it doesn't exist using native query
     */
    @Modifying
        @Query(value = "INSERT INTO platform_stats (id, total_users, active_users_last_7_days, active_users_last_30_days, " +
                "new_users_this_week, new_users_this_month, total_posts, total_questions, total_answers, total_comments, " +
                "posts_this_week, total_tasks, completed_tasks, total_connections, total_study_groups, active_study_groups, " +
                "total_points_awarded, total_badges_earned, last_updated) " +
                "SELECT 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, CURRENT_TIMESTAMP " +
                "WHERE NOT EXISTS (SELECT 1 FROM platform_stats WHERE id = 1)", nativeQuery = true)
    void initializeIfNotExists();

    // ==================== Atomic Increment Queries ====================

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalUsers = p.totalUsers + 1, " +
           "p.newUsersThisWeek = p.newUsersThisWeek + 1, " +
           "p.newUsersThisMonth = p.newUsersThisMonth + 1 WHERE p.id = 1")
    void incrementTotalUsers();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalPosts = p.totalPosts + 1, " +
           "p.postsThisWeek = p.postsThisWeek + 1 WHERE p.id = 1")
    void incrementTotalPosts();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalQuestions = p.totalQuestions + 1 WHERE p.id = 1")
    void incrementTotalQuestions();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalAnswers = p.totalAnswers + 1 WHERE p.id = 1")
    void incrementTotalAnswers();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalComments = p.totalComments + 1 WHERE p.id = 1")
    void incrementTotalComments();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalTasks = p.totalTasks + 1 WHERE p.id = 1")
    void incrementTotalTasks();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.completedTasks = p.completedTasks + 1 WHERE p.id = 1")
    void incrementCompletedTasks();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalConnections = p.totalConnections + 1 WHERE p.id = 1")
    void incrementTotalConnections();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalStudyGroups = p.totalStudyGroups + 1, " +
           "p.activeStudyGroups = p.activeStudyGroups + 1 WHERE p.id = 1")
    void incrementTotalStudyGroups();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalPointsAwarded = p.totalPointsAwarded + :points WHERE p.id = 1")
    void addPoints(long points);

    @Modifying
    @Query("UPDATE PlatformStats p SET p.totalBadgesEarned = p.totalBadgesEarned + 1 WHERE p.id = 1")
    void incrementBadgesEarned();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.activeUsersLast7Days = :last7Days, " +
           "p.activeUsersLast30Days = :last30Days WHERE p.id = 1")
    void updateActiveUsers(long last7Days, long last30Days);

    // ==================== Reset Queries ====================

    @Modifying
    @Query("UPDATE PlatformStats p SET p.newUsersThisWeek = 0, p.postsThisWeek = 0, " +
           "p.weekResetAt = CURRENT_TIMESTAMP WHERE p.id = 1")
    void resetWeeklyCounters();

    @Modifying
    @Query("UPDATE PlatformStats p SET p.newUsersThisMonth = 0, " +
           "p.monthResetAt = CURRENT_TIMESTAMP WHERE p.id = 1")
    void resetMonthlyCounters();
}
