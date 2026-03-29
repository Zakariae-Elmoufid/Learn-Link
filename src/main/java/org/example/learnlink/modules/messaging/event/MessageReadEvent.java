package org.example.learnlink.modules.messaging.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a message is read
 */
@Getter
public class MessageReadEvent extends ApplicationEvent {

    private final Long messageId;
    private final Long senderId;
    private final Long readerId;
    private final LocalDateTime readAt;

    public MessageReadEvent(Object source, Long messageId, Long senderId, Long readerId, LocalDateTime readAt) {
        super(source);
        this.messageId = messageId;
        this.senderId = senderId;
        this.readerId = readerId;
        this.readAt = readAt;
    }
}
