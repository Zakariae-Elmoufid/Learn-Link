package org.example.learnlink.modules.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.notification.event.NotificationEvent;
import org.example.learnlink.modules.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for NotificationEvent and creates notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.debug("Received notification event for user {}: {}", event.getUserId(), event.getType());

        notificationService.createNotification(
                event.getUserId(),
                event.getType(),
                event.getTitle(),
                event.getMessage(),
                event.getData()
        );
    }
}
