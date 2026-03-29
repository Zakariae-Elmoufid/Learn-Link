package org.example.learnlink.modules.messaging.exception;

import org.example.learnlink.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a message is not found
 */
public class MessageNotFoundException extends ResourceNotFoundException {

    public MessageNotFoundException(Long messageId) {
        super("Message", "id", messageId);
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}
