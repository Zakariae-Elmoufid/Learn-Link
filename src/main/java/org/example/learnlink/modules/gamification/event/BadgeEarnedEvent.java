package org.example.learnlink.modules.gamification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a badge is earned/awarded to a user.
 * Consumed by Admin module to track total badges across the platform.
 */
@Getter
public class BadgeEarnedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long badgeId;
    private final String badgeCode;

    public BadgeEarnedEvent(Object source, Long userId, Long badgeId, String badgeCode) {
        super(source);
        this.userId = userId;
        this.badgeId = badgeId;
        this.badgeCode = badgeCode;
    }
}
