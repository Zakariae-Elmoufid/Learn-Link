package org.example.learnlink.modules.gamification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.gamification.service.UserBadgeService;
import org.example.learnlink.modules.matching.event.ConnectionAcceptedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for matching module gamification events.
 * Awards points for social/networking activities.
 * All handlers are async to avoid blocking the main transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingGamificationListener {

    private final GamificationService gamificationService;
    private final UserBadgeService userBadgeService;

    /**
     * Handle connection accepted event - award points to both users
     */
    @Async
    @EventListener
    public void handleConnectionAcceptedEvent(ConnectionAcceptedEvent event) {
        log.info("Connection accepted event received: connectionId={}, user1={}, user2={}",
                event.getConnectionId(), event.getUser1Id(), event.getUser2Id());

        try {
            // Award points to both users for making a connection
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(15)
                    .actionType("CONNECTION_MADE")
                    .build();

            gamificationService.addPoints(event.getUser1Id(), request);
            gamificationService.addPoints(event.getUser2Id(), request);

            log.info("Points awarded for connection: user1={}, user2={}, points=15 each",
                    event.getUser1Id(), event.getUser2Id());

            // Award SOCIAL_BUTTERFLY badge (ID 4) for making connections
            userBadgeService.awardBadgeToUser(event.getUser1Id(), 4L);
            userBadgeService.awardBadgeToUser(event.getUser2Id(), 4L);
        } catch (Exception e) {
            log.error("Error awarding points for connection accepted: {}", e.getMessage());
        }
    }


}
