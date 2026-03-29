package org.example.learnlink.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatistics {
    private int totalPoints;
    private int level;
    private int pointsForNextLevel;
    private int currentLevelPoints;
    private long totalBadgesEarned;
    private long activeConnections;
    private long totalPostsCreated;
    private long totalQuestionsAsked;
    private long totalAnswersProvided;
    private long totalCommentsCreated;
    private long questionsResolved;
    private long answersAccepted;
}
