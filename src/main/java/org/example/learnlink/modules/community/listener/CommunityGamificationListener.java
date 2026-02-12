package org.example.learnlink.modules.community.listener;

import org.example.learnlink.modules.community.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for community gamification events
 * This listener publishes events to a gamification service for points and badges
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityGamificationListener {

    /**
     * Handle post created event - award points to author
     */
    @EventListener
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.info("Post created event received: userId={}, postId={}, postType={}",
            event.getUserId(), event.getPostId(), event.getPostType());

        // Award points based on post type
        int points = switch (event.getPostType()) {
            case SUMMARY -> 10;
            case TUTORIAL -> 15;
            case DISCUSSION -> 8;
        };

        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getUserId(), points, "POST_CREATED");
    }

    /**
     * Handle question asked event - award points to author
     */
    @EventListener
    public void handleQuestionAskedEvent(QuestionAskedEvent event) {
        log.info("Question asked event received: userId={}, questionId={}",
            event.getUserId(), event.getQuestionId());

        // Award points for asking question
        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getUserId(), 5, "QUESTION_ASKED");
    }

    /**
     * Handle answer provided event - award points to author
     */
    @EventListener
    public void handleAnswerProvidedEvent(AnswerProvidedEvent event) {
        log.info("Answer provided event received: userId={}, answerId={}, questionId={}",
            event.getUserId(), event.getAnswerId(), event.getQuestionId());

        // Award points for providing answer
        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getUserId(), 10, "ANSWER_PROVIDED");
    }

    /**
     * Handle answer accepted event - award bonus points to answer author
     */
    @EventListener
    public void handleAnswerAcceptedEvent(AnswerAcceptedEvent event) {
        log.info("Answer accepted event received: answerId={}, answerAuthorId={}, askerUserId={}",
            event.getAnswerId(), event.getAnswerAuthorId(), event.getQuestionAskerUserId());

        // Award significant points for accepted answer
        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getAnswerAuthorId(), 50, "ANSWER_ACCEPTED");

        // Award badge for helpful contributor if reaching threshold
        // TODO: Check and award "Helpful Contributor" badge
    }

    /**
     * Handle answer upvoted event - award points for helpful answer
     */
    @EventListener
    public void handleAnswerUpvotedEvent(AnswerUpvotedEvent event) {
        log.info("Answer upvoted event received: answerId={}, answerAuthorId={}, voterUserId={}",
            event.getAnswerId(), event.getAnswerAuthorId(), event.getVoterUserId());

        // Award small points for each upvote
        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getAnswerAuthorId(), 5, "ANSWER_UPVOTED");
    }

    /**
     * Handle comment added event - award points to author
     */
    @EventListener
    public void handleCommentAddedEvent(CommentAddedEvent event) {
        log.info("Comment added event received: userId={}, commentId={}, postId={}, answerId={}",
            event.getUserId(), event.getCommentId(), event.getPostId(), event.getAnswerId());

        // Award small points for commenting
        // TODO: Publish to gamification service
        // gamificationService.addPoints(event.getUserId(), 2, "COMMENT_ADDED");
    }
}

