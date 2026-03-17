package org.example.learnlink.modules.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.event.BadgeEarnedEvent;
import org.example.learnlink.modules.gamification.event.PointsAwardedEvent;
import org.example.learnlink.modules.notification.entity.NotificationType;
import org.example.learnlink.modules.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Listens for various domain events and triggers corresponding notifications.
 * Centralizes notification triggering logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerListener {

    private final ApplicationEventPublisher eventPublisher;
    private final org.example.learnlink.modules.community.repository.PostRepository postRepository;
    private final org.example.learnlink.modules.community.repository.QuestionRepository questionRepository;
    private final org.example.learnlink.modules.community.repository.AnswerRepository answerRepository;

    /**
     * Handle badge earned event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBadgeEarned(BadgeEarnedEvent event) {
        publish(event.getUserId(), NotificationType.BADGE_EARNED,
                "New Badge Earned!",
                "Congratulations! You earned the \"" + event.getBadgeCode() + "\" badge!",
                Map.of("badgeId", event.getBadgeId(), "badgeCode", event.getBadgeCode(), "link", "/profile/badges"));
    }

    /**
     * Handle points awarded event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointsAwarded(PointsAwardedEvent event) {
        if (event.getPoints() < 10) return;
        publish(event.getUserId(), NotificationType.POINTS_EARNED,
                "Points Earned!",
                "You earned " + event.getPoints() + " points for: " + event.getActionType(),
                Map.of("points", event.getPoints(), "action", event.getActionType(), "link", "/profile"));
    }

    /**
     * Handle post liked event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(org.example.learnlink.modules.community.event.PostLikedEvent event) {
        if (event.getLikerUserId().equals(event.getPostAuthorId())) return;
        publish(event.getPostAuthorId(), NotificationType.POST_LIKED,
                "Someone liked your post!",
                "A user liked your post.",
                Map.of("postId", event.getPostId(), "likerId", event.getLikerUserId(), "link", "/community/posts/" + event.getPostId()));
    }

    /**
     * Handle comment added event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(org.example.learnlink.modules.community.event.CommentAddedEvent event) {
        Long recipientId = null;
        String title = "New comment!";
        String link = "/";

        if (event.getPostId() != null) {
            recipientId = postRepository.findById(event.getPostId()).map(p -> p.getUserId()).orElse(null);
            title = "Someone commented on your post!";
            link = "/community/posts/" + event.getPostId();
        } else if (event.getAnswerId() != null) {
            recipientId = answerRepository.findById(event.getAnswerId()).map(a -> a.getUserId()).orElse(null);
            title = "Someone commented on your answer!";
            link = "/community/answers/" + event.getAnswerId();
        }

        if (recipientId != null && !recipientId.equals(event.getUserId())) {
            publish(recipientId, NotificationType.POST_COMMENTED, title, "A user added a comment.",
                    Map.of("commentId", event.getCommentId(), "link", link));
        }
    }

    /**
     * Handle answer provided event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnswerProvided(org.example.learnlink.modules.community.event.AnswerProvidedEvent event) {
        Long askerId = questionRepository.findById(event.getQuestionId()).map(q -> q.getUserId()).orElse(null);
        if (askerId != null && !askerId.equals(event.getUserId())) {
            publish(askerId, NotificationType.QUESTION_ANSWERED,
                    "New answer to your question!",
                    "Someone provided an answer to your question.",
                    Map.of("questionId", event.getQuestionId(), "answerId", event.getAnswerId(), "link", "/community/questions/" + event.getQuestionId()));
        }
    }

    /**
     * Handle answer accepted event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnswerAccepted(org.example.learnlink.modules.community.event.AnswerAcceptedEvent event) {
        if (!event.getAnswerAuthorId().equals(event.getQuestionAskerUserId())) {
            publish(event.getAnswerAuthorId(), NotificationType.ANSWER_ACCEPTED,
                    "Your answer was accepted!",
                    "Congratulations! Your answer was marked as the best answer.",
                    Map.of("answerId", event.getAnswerId(), "link", "/community/answers/" + event.getAnswerId()));
        }
    }

    /**
     * Handle connection request sent event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConnectionRequest(org.example.learnlink.modules.matching.event.ConnectionRequestSentEvent event) {
        publish(event.getReceiverId(), NotificationType.CONNECTION_REQUEST,
                "New Connection Request",
                "Someone wants to connect with you.",
                Map.of("requestId", event.getRequestId(), "senderId", event.getSenderId(), "link", "/connections/requests"));
    }

    /**
     * Handle connection accepted event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConnectionAccepted(org.example.learnlink.modules.matching.event.ConnectionAcceptedEvent event) {
        // Notify the person who sent the request (user1)
        publish(event.getUser1Id(), NotificationType.CONNECTION_ACCEPTED,
                "Connection Request Accepted",
                "Your connection request was accepted!",
                Map.of("connectionId", event.getConnectionId(), "user2Id", event.getUser2Id(), "link", "/connections"));
    }

    /**
     * Handle answer upvoted event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnswerUpvoted(org.example.learnlink.modules.community.event.AnswerUpvotedEvent event) {
        if (event.getVoterUserId().equals(event.getAnswerAuthorId())) return;
        publish(event.getAnswerAuthorId(), NotificationType.ANSWER_VOTED,
                "Someone upvoted your answer!",
                "A user liked your answer on a question.",
                Map.of("answerId", event.getAnswerId(), "voterId", event.getVoterUserId(), "link", "/community/answers/" + event.getAnswerId()));
    }

    private void publish(Long userId, NotificationType type, String title, String message, Map<String, Object> data) {
        eventPublisher.publishEvent(
                NotificationEvent.builder(this)
                        .userId(userId)
                        .type(type)
                        .title(title)
                        .message(message)
                        .data(data)
                        .build()
        );
    }
}
