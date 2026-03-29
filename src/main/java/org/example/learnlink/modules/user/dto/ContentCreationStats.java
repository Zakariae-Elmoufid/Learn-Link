package org.example.learnlink.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentCreationStats {
    private long totalPostsCreated;
    private long totalQuestionsAsked;
    private long totalAnswersProvided;
    private long totalCommentsCreated;
    private long totalPostLikes;
    private long totalAnswersAccepted;
    private long questionsResolved;
    private long averageLikesPerPost;
    private long averageCommentsPerQuestion;
    private long engagementScore; // calculated metric
}
