package org.example.learnlink.modules.messaging.service;

import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for group messaging operations.
 */
public interface IGroupMessageService {

    /**
     * Send a message to a group.
     * 
     * @param senderId ID of the sender
     * @param groupId ID of the study group
     * @param request Message content and metadata
     * @return The created message response
     * @throws org.example.learnlink.modules.messaging.exception.GroupAccessDeniedException if user is not a member of the group
     * @throws org.example.learnlink.modules.messaging.exception.GroupNotFoundException if group does not exist
     */
    GroupMessageResponse sendMessage(Long senderId, Long groupId, GroupMessageRequest request);

    /**
     * Get paginated message history for a group.
     * 
     * @param userId ID of the requesting user (for membership validation)
     * @param groupId ID of the study group
     * @param pageable Pagination parameters
     * @return Page of messages, newest first
     */
    Page<GroupMessageResponse> getGroupMessages(Long userId, Long groupId, Pageable pageable);

    /**
     * Mark all messages in a group as read for a user.
     * 
     * @param userId ID of the user
     * @param groupId ID of the study group
     * @return Number of messages marked as read
     */
    int markAllAsRead(Long userId, Long groupId);

    /**
     * Mark a specific message as read.
     * 
     * @param userId ID of the user
     * @param messageId ID of the message
     */
    void markMessageAsRead(Long userId, Long messageId);

    /**
     * Get unread message count for a user in a group.
     * 
     * @param userId ID of the user
     * @param groupId ID of the study group
     * @return Number of unread messages
     */
    Long getUnreadCount(Long userId, Long groupId);

    /**
     * Get list of user IDs who have read a message.
     * 
     * @param messageId ID of the message
     * @return List of user IDs who read the message
     */
    List<Long> getMessageReaders(Long messageId);
}
