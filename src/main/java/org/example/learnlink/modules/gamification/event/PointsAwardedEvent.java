package org.example.learnlink.modules.gamification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when points are awarded to a user.
 * Consumed by Admin module to track total points across the platform.
 */
@Getter
public class PointsAwardedEvent extends ApplicationEvent {

    private final Long userId;
    private final int points;
    private final String actionType;

    public PointsAwardedEvent(Object source, Long userId, int points, String actionType) {
        super(source);
        this.userId = userId;
        this.points = points;
        this.actionType = actionType;
    }
}
