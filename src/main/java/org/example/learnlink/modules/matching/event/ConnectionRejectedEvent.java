package org.example.learnlink.modules.matching.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a connection request is rejected.
 * Can be consumed by:
 * - Notification module: to notify the sender (optional, depends on business rules)
 */
@Getter
public class ConnectionRejectedEvent extends ApplicationEvent {

    private final Long requestId;
    private final Long senderId;
    private final Long receiverId;

    public ConnectionRejectedEvent(Object source, Long requestId,
                                    Long senderId, Long receiverId) {
        super(source);
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}
