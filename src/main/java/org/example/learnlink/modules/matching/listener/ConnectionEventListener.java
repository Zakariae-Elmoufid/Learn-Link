package org.example.learnlink.modules.matching.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.matching.event.ConnectionAcceptedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRejectedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRemovedEvent;
import org.example.learnlink.modules.matching.event.ConnectionRequestSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for connection-related events within the matching module.
 * Handles logging and any internal matching module reactions to events.
 * 
 * Note: For cross-module communication (e.g., notifications, gamification),
 * create listeners in those respective modules that consume these events.
 * 
 * Example listener in notification module:
 * @Component
 * public class MatchingNotificationListener {
 *     @EventListener
 *     @Async
 *     public void onConnectionRequestSent(ConnectionRequestSentEvent event) {
 *         notificationService.createNotification(
 *             event.getReceiverId(),
 *             "CONNECTION_REQUEST",
 *             "You have a new connection request"
 *         );
 *     }
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionEventListener {

    /**
     * Handle connection request sent event.
     * Log the event and perform any matching module specific actions.
     *
     * @param event the connection request sent event
     */
    @EventListener
    @Async
    public void onConnectionRequestSent(ConnectionRequestSentEvent event) {
        log.info("Connection request sent: requestId={}, from user {} to user {}, compatibility={}%",
                event.getRequestId(),
                event.getSenderId(),
                event.getReceiverId(),
                event.getCompatibilityScore());

        // Additional matching module logic can be added here
        // For example: update matching suggestions cache, analytics, etc.
    }

    /**
     * Handle connection accepted event.
     * Log the event and perform any matching module specific actions.
     *
     * @param event the connection accepted event
     */
    @EventListener
    @Async
    public void onConnectionAccepted(ConnectionAcceptedEvent event) {
        log.info("Connection accepted: connectionId={}, requestId={}, between users {} and {}, compatibility={}%",
                event.getConnectionId(),
                event.getRequestId(),
                event.getUser1Id(),
                event.getUser2Id(),
                event.getCompatibilityScore());

        // Additional matching module logic can be added here
        // For example: update matching suggestions to exclude connected users
    }

    /**
     * Handle connection rejected event.
     * Log the event and perform any matching module specific actions.
     *
     * @param event the connection rejected event
     */
    @EventListener
    @Async
    public void onConnectionRejected(ConnectionRejectedEvent event) {
        log.info("Connection rejected: requestId={}, sender={}, receiver={}",
                event.getRequestId(),
                event.getSenderId(),
                event.getReceiverId());

        // Additional matching module logic can be added here
        // For example: adjust matching algorithm based on rejections
    }

    /**
     * Handle connection removed event.
     * Log the event and perform any matching module specific actions.
     *
     * @param event the connection removed event
     */
    @EventListener
    @Async
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        log.info("Connection removed: connectionId={}, between users {} and {}, removed by user {}",
                event.getConnectionId(),
                event.getUser1Id(),
                event.getUser2Id(),
                event.getRemovedByUserId());

        // Additional matching module logic can be added here
        // For example: update matching suggestions to re-include the user
    }
}
