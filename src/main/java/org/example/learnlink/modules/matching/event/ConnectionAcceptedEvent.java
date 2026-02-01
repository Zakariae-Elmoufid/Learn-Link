package org.example.learnlink.modules.matching.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Event published when a connection request is accepted.
 * This means a new connection is established between two users.
 * Can be consumed by:
 * - Notification module: to notify the original sender
 * - Gamification module: to award points to both users
 * - Messaging module: to enable direct messaging
 */
@Getter
public class ConnectionAcceptedEvent extends ApplicationEvent {

    private final Long connectionId;
    private final Long requestId;
    private final Long user1Id;
    private final Long user2Id;
    private final BigDecimal compatibilityScore;

    public ConnectionAcceptedEvent(Object source, Long connectionId, Long requestId,
                                    Long user1Id, Long user2Id,
                                    BigDecimal compatibilityScore) {
        super(source);
        this.connectionId = connectionId;
        this.requestId = requestId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.compatibilityScore = compatibilityScore;
    }
}
