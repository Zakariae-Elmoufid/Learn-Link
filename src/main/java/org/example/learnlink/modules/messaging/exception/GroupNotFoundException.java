package org.example.learnlink.modules.messaging.exception;

import org.example.learnlink.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a study group is not found
 */
public class GroupNotFoundException extends ResourceNotFoundException {

    public GroupNotFoundException(Long groupId) {
        super("Study Group", "id", groupId);
    }

    public GroupNotFoundException(String message) {
        super(message);
    }
}
