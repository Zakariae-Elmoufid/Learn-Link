package org.example.learnlink.modules.messaging.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.messaging.event.GroupMessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for group messaging events.
 * Handles gamification and other cross-cutting concerns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupMessageEventListener {

    private final GamificationService gamificationService;

    /**
     * Award points for sending a group message.
     * Points: 2 points per group message (encourages participation)
     */
    @Async
    @EventListener
    public void handleGroupMessageSent(GroupMessageSentEvent event) {
        log.debug("Processing GroupMessageSentEvent for message {} in group {}", 
                event.getMessageId(), event.getGroupId());

        try {
            // Award 2 points for group message participation
            AddPointsRequest pointsRequest = AddPointsRequest.builder()
                    .actionType("GROUP_MESSAGE")
                    .points(2)
                    .description("Sent message in study group")
                    .build();
            
            gamificationService.addPoints(event.getSenderId(), pointsRequest);
            
            log.debug("Awarded 2 points to user {} for group message", event.getSenderId());
        } catch (Exception e) {
            log.error("Failed to process group message event for user {}", event.getSenderId(), e);
        }
    }
}
