package org.example.learnlink.modules.messaging.exception;

/**
 * Exception thrown when a user tries to access a group they are not a member of
 */
public class GroupAccessDeniedException extends RuntimeException {

    public GroupAccessDeniedException(Long groupId, Long userId) {
        super(String.format("User %d is not a member of group %d", userId, groupId));
    }

    public GroupAccessDeniedException(String message) {
        super(message);
    }
}
