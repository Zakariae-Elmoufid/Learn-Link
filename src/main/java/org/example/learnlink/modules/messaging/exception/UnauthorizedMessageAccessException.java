package org.example.learnlink.modules.messaging.exception;

/**
 * Exception thrown when user is not authorized to access a message
 */
public class UnauthorizedMessageAccessException extends RuntimeException {

    public UnauthorizedMessageAccessException(String message) {
        super(message);
    }

    public UnauthorizedMessageAccessException(Long messageId, Long userId) {
        super(String.format("User %d is not authorized to access message %d", userId, messageId));
    }
}
