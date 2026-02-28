package org.example.learnlink.modules.admin.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    
    // User Statistics
    private Long totalUsers;
    private Long activeUsersLast7Days;
    private Long activeUsersLast30Days;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    
    // Content Statistics
    private Long totalPosts;
    private Long totalQuestions;
    private Long totalAnswers;
    private Long totalComments;
    private Long postsThisWeek;
    
    // Task Statistics
    private Long totalTasks;
    private Long completedTasks;
    private Double taskCompletionRate;
    
    // Engagement Statistics
    private Long totalConnections;
    private Long totalStudyGroups;
    private Long activeStudySessions;
    
    // Gamification Statistics
    private Long totalPointsAwarded;
    private Long badgesEarned;
    
    // Top Subjects
    private List<SubjectStatDto> topSubjects;
    
    // Timestamp
    private LocalDateTime generatedAt;
}
