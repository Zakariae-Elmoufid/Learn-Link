package org.example.learnlink.modules.messaging.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event published when a group message is sent.
 * Used for:
 * - WebSocket broadcast
 * - Gamification points
 * - Notifications
 */
@Getter
public class GroupMessageSentEvent extends ApplicationEvent {
    
    private final Long messageId;
    private final Long groupId;
    private final Long senderId;
    private final List<Long> memberIds;

    public GroupMessageSentEvent(Object source, Long messageId, Long groupId, 
                                  Long senderId, List<Long> memberIds) {
        super(source);
        this.messageId = messageId;
        this.groupId = groupId;
        this.senderId = senderId;
        this.memberIds = memberIds;
    }
}
