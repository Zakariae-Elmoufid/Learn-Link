package org.example.learnlink.modules.matching.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a connection is removed/deleted.
 * Can be consumed by:
 * - Messaging module: to handle conversation visibility
 * - Other modules that track connections
 */
@Getter
public class ConnectionRemovedEvent extends ApplicationEvent {

    private final Long connectionId;
    private final Long user1Id;
    private final Long user2Id;
    private final Long removedByUserId;

    public ConnectionRemovedEvent(Object source, Long connectionId,
                                   Long user1Id, Long user2Id,
                                   Long removedByUserId) {
        super(source);
        this.connectionId = connectionId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.removedByUserId = removedByUserId;
    }
}
