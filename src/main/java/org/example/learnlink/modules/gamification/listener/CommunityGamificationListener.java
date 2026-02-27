package org.example.learnlink.modules.gamification.listener;

import org.example.learnlink.modules.community.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.gamification.service.UserBadgeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for community gamification events.
 * This listener publishes events to a gamification service for points and badges.
 * All handlers are async to avoid blocking the main transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityGamificationListener {

    private final GamificationService gamificationService;
    private final UserBadgeService userBadgeService;


    /**
     * Handle post created event - award points to author
     */
    @Async
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.info("Post created event received: userId={}, postId={}, postType={}",
            event.getUserId(), event.getPostId(), event.getPostType());

        try {
            // Award points based on post type
            int points = switch (event.getPostType()) {
                case SUMMARY -> 10;
                case TUTORIAL -> 15;
                case DISCUSSION -> 8;
                default -> 5;
            };

            AddPointsRequest request = AddPointsRequest.builder()
                    .points(points)
                    .actionType("POST_CREATED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for post creation: userId={}, points={}",
                event.getUserId(), points);

            // Check if user has earned FIRST_POST badge
            userBadgeService.awardBadgeToUser(event.getUserId(), 1L); // FIRST_POST
        } catch (Exception e) {
            log.error("Error awarding points for post creation: {}", e.getMessage());
        }
    }

    /**
     * Handle question asked event - award points to author
     */
    @Async
    @EventListener
    public void handleQuestionAskedEvent(QuestionAskedEvent event) {
        log.info("Question asked event received: userId={}, questionId={}",
            event.getUserId(), event.getQuestionId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(5)
                    .actionType("QUESTION_ASKED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for question asked: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error awarding points for question asked: {}", e.getMessage());
        }
    }

    /**
     * Handle answer provided event - award points to author
     */
    @Async
    @EventListener
    public void handleAnswerProvidedEvent(AnswerProvidedEvent event) {
        log.info("Answer provided event received: userId={}, answerId={}, questionId={}",
            event.getUserId(), event.getAnswerId(), event.getQuestionId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(10)
                    .actionType("ANSWER_PROVIDED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for answer provided: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error awarding points for answer provided: {}", e.getMessage());
        }
    }

    /**
     * Handle answer accepted event - award bonus points to answer author
     */
    @Async
    @EventListener
    public void handleAnswerAcceptedEvent(AnswerAcceptedEvent event) {
        log.info("Answer accepted event received: answerId={}, answerAuthorId={}, askerUserId={}",
            event.getAnswerId(), event.getAnswerAuthorId(), event.getQuestionAskerUserId());

        try {
            // Award significant points for accepted answer
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(25)
                    .actionType("ANSWER_ACCEPTED")
                    .build();

            gamificationService.addPoints(event.getAnswerAuthorId(), request);
            log.info("Points awarded for accepted answer: userId={}", event.getAnswerAuthorId());

            // Award badge for helpful contributor (HELPFUL_EXPERT - ID 2)
            userBadgeService.awardBadgeToUser(event.getAnswerAuthorId(), 2L);
        } catch (Exception e) {
            log.error("Error awarding points for accepted answer: {}", e.getMessage());
        }
    }

    /**
     * Handle answer upvoted event - award points for helpful answer
     */
    @Async
    @EventListener
    public void handleAnswerUpvotedEvent(AnswerUpvotedEvent event) {
        log.info("Answer upvoted event received: answerId={}, answerAuthorId={}, voterUserId={}",
            event.getAnswerId(), event.getAnswerAuthorId(), event.getVoterUserId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(2)
                    .actionType("ANSWER_UPVOTED")
                    .build();

            gamificationService.addPoints(event.getAnswerAuthorId(), request);
            log.info("Points awarded for answer upvote: userId={}", event.getAnswerAuthorId());
        } catch (Exception e) {
            log.error("Error awarding points for answer upvote: {}", e.getMessage());
        }
    }

    /**
     * Handle comment added event - award points to author
     */
    @Async
    @EventListener
    public void handleCommentAddedEvent(CommentAddedEvent event) {
        log.info("Comment added event received: userId={}, commentId={}, postId={}, answerId={}",
            event.getUserId(), event.getCommentId(), event.getPostId(), event.getAnswerId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(2)
                    .actionType("COMMENT_ADDED")
                    .build();

            gamificationService.addPoints(event.getUserId(), request);
            log.info("Points awarded for comment added: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error awarding points for comment added: {}", e.getMessage());
        }
    }

    /**
     * Handle post liked event - award points to post author
     */
    @Async
    @EventListener
    public void handlePostLikedEvent(PostLikedEvent event) {
        log.info("Post liked event received: postId={}, postAuthorId={}, likerUserId={}",
            event.getPostId(), event.getPostAuthorId(), event.getLikerUserId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(1)
                    .actionType("POST_LIKED")
                    .build();

            gamificationService.addPoints(event.getPostAuthorId(), request);
            log.info("Points awarded for post like: userId={}", event.getPostAuthorId());
        } catch (Exception e) {
            log.error("Error awarding points for post like: {}", e.getMessage());
        }
    }
}

