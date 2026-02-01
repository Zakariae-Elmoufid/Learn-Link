package org.example.learnlink.modules.matching.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Event published when a user sends a connection request to another user.
 * Can be consumed by:
 * - Notification module: to notify the receiver
 * - Gamification module: to award points (optional)
 */
@Getter
public class ConnectionRequestSentEvent extends ApplicationEvent {

    private final Long requestId;
    private final Long senderId;
    private final Long receiverId;
    private final BigDecimal compatibilityScore;

    public ConnectionRequestSentEvent(Object source, Long requestId,
                                       Long senderId, Long receiverId,
                                       BigDecimal compatibilityScore) {
        super(source);
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.compatibilityScore = compatibilityScore;
    }
}
