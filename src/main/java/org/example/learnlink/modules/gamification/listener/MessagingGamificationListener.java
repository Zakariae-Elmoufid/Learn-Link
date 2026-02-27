package org.example.learnlink.modules.gamification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.gamification.dto.AddPointsRequest;
import org.example.learnlink.modules.gamification.service.GamificationService;
import org.example.learnlink.modules.messaging.event.GroupMessageSentEvent;
import org.example.learnlink.modules.messaging.event.MessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for messaging module gamification events.
 * Awards points for communication activities.
 * All handlers are async to avoid blocking message delivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingGamificationListener {

    private final GamificationService gamificationService;

    /**
     * Handle direct message sent event - award points for communication
     */
    @Async
    @EventListener
    public void handleMessageSentEvent(MessageSentEvent event) {
        log.debug("Message sent event received: senderId={}, messageId={}",
                event.getSenderId(), event.getMessageId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(1)
                    .actionType("MESSAGE_SENT")
                    .build();

            gamificationService.addPoints(event.getSenderId(), request);
            log.debug("Points awarded for message sent: userId={}, points=1",
                    event.getSenderId());
        } catch (Exception e) {
            log.error("Error awarding points for message sent: {}", e.getMessage());
        }
    }

    /**
     * Handle group message sent event - award points for group participation
     */
    @Async
    @EventListener
    public void handleGroupMessageSentEvent(GroupMessageSentEvent event) {
        log.debug("Group message sent event received: senderId={}, groupId={}, messageId={}",
                event.getSenderId(), event.getGroupId(), event.getMessageId());

        try {
            AddPointsRequest request = AddPointsRequest.builder()
                    .points(1)
                    .actionType("GROUP_MESSAGE_SENT")
                    .build();

            gamificationService.addPoints(event.getSenderId(), request);
            log.debug("Points awarded for group message sent: userId={}, points=1",
                    event.getSenderId());
        } catch (Exception e) {
            log.error("Error awarding points for group message sent: {}", e.getMessage());
        }
    }
}
