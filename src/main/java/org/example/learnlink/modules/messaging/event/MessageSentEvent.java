package org.example.learnlink.modules.messaging.event;

import lombok.Getter;
import org.example.learnlink.modules.messaging.entity.MessageType;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a message is sent
 */
@Getter
public class MessageSentEvent extends ApplicationEvent {

    private final Long messageId;
    private final Long senderId;
    private final Long recipientId;
    private final MessageType messageType;

    public MessageSentEvent(Object source, Long messageId, Long senderId, Long recipientId, MessageType messageType) {
        super(source);
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageType = messageType;
    }
}
