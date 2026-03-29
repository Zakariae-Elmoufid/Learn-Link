package org.example.learnlink.modules.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.community.entity.Answer;
import org.example.learnlink.modules.community.entity.Comment;
import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.entity.Question;
import org.example.learnlink.modules.community.repository.AnswerRepository;
import org.example.learnlink.modules.community.repository.CommentRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import org.example.learnlink.modules.community.repository.QuestionRepository;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.example.learnlink.modules.matching.entity.Connection;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.example.learnlink.modules.user.dto.ContentCreationStats;
import org.example.learnlink.modules.user.dto.DashboardStatistics;
import org.example.learnlink.modules.user.dto.RecentActivityItem;
import org.example.learnlink.modules.user.dto.StudentDashboardResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentDashboardServiceImpl implements IStudentDashboardService {

    private final UserScoreRepository userScoreRepository;
    private final PostRepository postRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final ConnectionRepository connectionRepository;

    @Override
    public StudentDashboardResponse getDashboard(Long userId) {
        return getDashboard(userId, 10);
    }

    @Override
    public StudentDashboardResponse getDashboard(Long userId, int activityLimit) {
        try {
            DashboardStatistics statistics = buildStatistics(userId);
            List<RecentActivityItem> recentActivities = buildRecentActivities(userId, activityLimit);
            ContentCreationStats contentStats = buildContentCreationStats(userId);

            return new StudentDashboardResponse(statistics, recentActivities, contentStats);
        } catch (Exception e) {
            log.error("Error building dashboard for user {}: {}", userId, e.getMessage(), e);
            return new StudentDashboardResponse(
                    new DashboardStatistics(),
                    new ArrayList<>(),
                    new ContentCreationStats()
            );
        }
    }

    private DashboardStatistics buildStatistics(Long userId) {
        DashboardStatistics stats = new DashboardStatistics();

        try {
            UserScore userScore = userScoreRepository.findByUserId(userId).orElse(null);
            if (userScore != null) {
                stats.setTotalPoints(userScore.getTotalPoints());
                stats.setLevel(userScore.getLevel());
                stats.setCurrentLevelPoints(userScore.getCurrentLevelPoints());
                stats.setPointsForNextLevel(userScore.getPointsForNextLevel());
            }

            stats.setTotalBadgesEarned(userScoreRepository.countBadgesEarnedByUserId(userId));
            stats.setActiveConnections(connectionRepository.countActiveConnectionsByUserId(userId));
            stats.setTotalPostsCreated(postRepository.countByUserId(userId));
            stats.setTotalQuestionsAsked(questionRepository.countByUserId(userId));
            stats.setTotalAnswersProvided(answerRepository.countByUserId(userId));
            stats.setTotalCommentsCreated(commentRepository.countByUserId(userId));
            stats.setQuestionsResolved(questionRepository.countResolvedQuestionsByUserId(userId));
            stats.setAnswersAccepted(answerRepository.countAcceptedAnswersByUserId(userId));
        } catch (Exception e) {
            log.warn("Error building statistics for user {}: {}", userId, e.getMessage());
        }

        return stats;
    }

    private List<RecentActivityItem> buildRecentActivities(Long userId, int limit) {
        List<RecentActivityItem> activities = new ArrayList<>();

        try {
            // Fetch ALL recent posts (no limit on individual fetches)
            List<Post> recentPosts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
            for (Post post : recentPosts) {
                RecentActivityItem item = new RecentActivityItem();
                item.setType("post");
                item.setTitle(post.getTitle());
                String postContent = post.getContent();
                item.setDescription(postContent.length() > 100 ? 
                        postContent.substring(0, Math.min(100, postContent.length())) + "..." : postContent);
                item.setCreatedAt(post.getCreatedAt());
                item.setPointsEarned(10);
                item.setBadgeColor("gold");
                activities.add(item);
            }

            // Fetch ALL recent questions (no limit on individual fetches)
            List<Question> recentQuestions = questionRepository.findByUserIdAndHiddenIsFalseOrderByCreatedAtDesc(userId);
            for (Question question : recentQuestions) {
                RecentActivityItem item = new RecentActivityItem();
                item.setType("question");
                item.setTitle("asked a question");
                String questionContent = question.getContent();
                item.setDescription(questionContent.length() > 100 ?
                        questionContent.substring(0, Math.min(100, questionContent.length())) + "..." : questionContent);
                item.setCreatedAt(question.getCreatedAt());
                item.setPointsEarned(5);
                item.setBadgeColor("silver");
                activities.add(item);
            }

            // Fetch ALL recent answers (no limit on individual fetches)
            List<Answer> recentAnswers = answerRepository.findByUserIdAndHiddenIsFalseOrderByCreatedAtDesc(userId);
            for (Answer answer : recentAnswers) {
                RecentActivityItem item = new RecentActivityItem();
                item.setType("answer");
                item.setTitle("Answered Question");
                String answerContent = answer.getContent();
                item.setDescription(answerContent.length() > 100 ? 
                        answerContent.substring(0, Math.min(100, answerContent.length())) + "..." : answerContent);
                item.setCreatedAt(answer.getCreatedAt());
                item.setPointsEarned(answer.getIsAccepted() ? 25 : 10);
                item.setBadgeColor(answer.getIsAccepted() ? "gold" : "silver");
                activities.add(item);
            }

            // Fetch ALL recent comments (no limit on individual fetches)
            List<Comment> recentComments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
            for (Comment comment : recentComments) {
                RecentActivityItem item = new RecentActivityItem();
                item.setType("comment");
                item.setTitle("Added Comment");
                String commentContent = comment.getContent();
                item.setDescription(commentContent.length() > 100 ? 
                        commentContent.substring(0, Math.min(100, commentContent.length())) + "..." : commentContent);
                item.setCreatedAt(comment.getCreatedAt());
                item.setPointsEarned(1);
                item.setBadgeColor("bronze");
                activities.add(item);
            }

            // Fetch ALL recent connections (no limit on individual fetches)
            List<Connection> recentConnections = connectionRepository.findByUser1IdOrderByCreatedAtDesc(userId);
            for (Connection connection : recentConnections) {
                RecentActivityItem item = new RecentActivityItem();
                item.setType("connection");
                item.setTitle("New Connection");
                item.setDescription("Connected with another learner");
                item.setCreatedAt(connection.getConnectedAt());
                item.setPointsEarned(15);
                item.setBadgeColor("gold");
                activities.add(item);
            }

            // Sort by date descending and THEN limit to requested number
            activities = activities.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(limit)
                    .collect(Collectors.toList());
            activities.stream().forEach(a -> log.info("Activity: {} - {} - {}", a.getType(), a.getTitle(), a.getCreatedAt()));

        } catch (Exception e) {
            log.warn("Error building recent activities for user {}: {}", userId, e.getMessage());
        }

        return activities;
    }

    private ContentCreationStats buildContentCreationStats(Long userId) {
        ContentCreationStats stats = new ContentCreationStats();

        try {
            long totalPosts = postRepository.countByUserId(userId);
            long totalQuestions = questionRepository.countByUserId(userId);
            long totalAnswers = answerRepository.countByUserId(userId);
            long totalComments = commentRepository.countByUserId(userId);

            stats.setTotalPostsCreated(totalPosts);
            stats.setTotalQuestionsAsked(totalQuestions);
            stats.setTotalAnswersProvided(totalAnswers);
            stats.setTotalCommentsCreated(totalComments);

            // Calculate engagement metrics
            stats.setTotalPostLikes(postRepository.sumLikesCountByUserId(userId));
            stats.setTotalAnswersAccepted(answerRepository.countAcceptedAnswersByUserId(userId));
            stats.setQuestionsResolved(questionRepository.countResolvedQuestionsByUserId(userId));

            // Calculate averages
            stats.setAverageLikesPerPost(totalPosts > 0 ? stats.getTotalPostLikes() / totalPosts : 0);
            stats.setAverageCommentsPerQuestion(totalQuestions > 0 ? totalComments / totalQuestions : 0);

            // Engagement score (weighted calculation)
            long engagementScore = (totalPosts * 10) + (totalQuestions * 5) + (totalAnswers * 10) +
                                  (totalComments * 1) + (stats.getTotalPostLikes() * 2) +
                                  (stats.getTotalAnswersAccepted() * 25);
            stats.setEngagementScore(engagementScore);

        } catch (Exception e) {
            log.warn("Error building content creation stats for user {}: {}", userId, e.getMessage());
        }

        return stats;
    }
}
