package org.example.learnlink.modules.admin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.admin.repository.PlatformStatsRepository;
import org.example.learnlink.modules.auth.event.OnUserRegisteredEvent;
import org.example.learnlink.modules.community.event.*;
import org.example.learnlink.modules.gamification.event.BadgeEarnedEvent;
import org.example.learnlink.modules.gamification.event.PointsAwardedEvent;
import org.example.learnlink.modules.matching.event.ConnectionAcceptedEvent;
import org.example.learnlink.modules.planner.event.TaskCompletedEvent;
import org.example.learnlink.modules.planner.event.TaskCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that updates platform statistics based on events from other modules.
 * This creates a loosely coupled architecture where the Admin module doesn't need
 * to directly access other modules' repositories.
 * 
 * Uses @TransactionalEventListener with AFTER_COMMIT to ensure the original transaction
 * has committed before updating stats, and REQUIRES_NEW to run in its own transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminStatsEventListener {

    private final PlatformStatsRepository platformStatsRepository;

    // ==================== User Events ====================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegistered(OnUserRegisteredEvent event) {
        log.info("Admin stats: User registered - userId={}", event.getUser().getId());
        try {
            platformStatsRepository.incrementTotalUsers();
            log.info("Admin stats: Updated user count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update user count", e);
        }
    }

    // ==================== Post/Content Events ====================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("Admin stats: Post created - postId={}, userId={}", event.getPostId(), event.getUserId());
        try {
            platformStatsRepository.incrementTotalPosts();
            log.info("Admin stats: Updated post count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update post count", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleQuestionAsked(QuestionAskedEvent event) {
        log.info("Admin stats: Question asked - questionId={}, userId={}", event.getQuestionId(), event.getUserId());
        try {
            platformStatsRepository.incrementTotalQuestions();
            log.info("Admin stats: Updated question count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update question count", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAnswerProvided(AnswerProvidedEvent event) {
        log.info("Admin stats: Answer provided - answerId={}, userId={}", event.getAnswerId(), event.getUserId());
        try {
            platformStatsRepository.incrementTotalAnswers();
            log.info("Admin stats: Updated answer count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update answer count", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("Admin stats: Comment added - commentId={}, userId={}", event.getCommentId(), event.getUserId());
        try {
            platformStatsRepository.incrementTotalComments();
            log.info("Admin stats: Updated comment count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update comment count", e);
        }
    }

    // ==================== Task Events ====================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("Admin stats: Task created - taskId={}, userId={}", event.getTaskId(), event.getUserId());
        try {
            platformStatsRepository.incrementTotalTasks();
            log.info("Admin stats: Updated task count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update task count", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        log.info("Admin stats: Task completed - taskId={}, userId={}", event.getTaskId(), event.getUserId());
        try {
            platformStatsRepository.incrementCompletedTasks();
            log.info("Admin stats: Updated completed task count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update completed task count", e);
        }
    }

    // ==================== Social/Matching Events ====================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleConnectionAccepted(ConnectionAcceptedEvent event) {
        log.info("Admin stats: Connection accepted - connectionId={}, user1={}, user2={}",
                event.getConnectionId(), event.getUser1Id(), event.getUser2Id());
        try {
            platformStatsRepository.incrementTotalConnections();
            log.info("Admin stats: Updated connection count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update connection count", e);
        }
    }

    // ==================== Gamification Events ====================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePointsAwarded(PointsAwardedEvent event) {
        log.info("Admin stats: Points awarded - userId={}, points={}, action={}",
                event.getUserId(), event.getPoints(), event.getActionType());
        try {
            platformStatsRepository.addPoints(event.getPoints());
            log.info("Admin stats: Updated total points successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update points", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBadgeEarned(BadgeEarnedEvent event) {
        log.info("Admin stats: Badge earned - userId={}, badgeId={}, code={}",
                event.getUserId(), event.getBadgeId(), event.getBadgeCode());
        try {
            platformStatsRepository.incrementBadgesEarned();
            log.info("Admin stats: Updated badge count successfully");
        } catch (Exception e) {
            log.error("Admin stats: Failed to update badge count", e);
        }
    }
}
