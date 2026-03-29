package org.example.learnlink.modules.messaging.exception;

/**
 * Exception thrown when a user tries to access a message they are not authorized to access
 */
public class UnauthorizedMessageAccessException extends RuntimeException {

    public UnauthorizedMessageAccessException(Long messageId, Long userId) {
        super(String.format("User %d is not authorized to access message %d", userId, messageId));
    }

    public UnauthorizedMessageAccessException(String message) {
        super(message);
    }
}
