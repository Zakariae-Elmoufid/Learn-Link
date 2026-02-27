package org.example.learnlink.modules.messaging.service;

import org.example.learnlink.modules.messaging.dto.ConversationResponse;
import org.example.learnlink.modules.messaging.dto.MessageResponse;
import org.example.learnlink.modules.messaging.dto.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface for message service
 */
public interface IMessageService {

    /**
     * Send a new message
     */
    MessageResponse sendMessage(Long senderId, SendMessageRequest request);

    /**
     * Get message by ID
     */
    MessageResponse getMessageById(Long messageId, Long userId);

    /**
     * Get conversation between two users
     */
    Page<MessageResponse> getConversation(Long userId, Long otherUserId, Pageable pageable);

    /**
     * Get all conversations for a user
     */
    List<ConversationResponse> getUserConversations(Long userId);

    /**
     * Mark message as read
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * Mark all messages from sender as read
     */
    void markConversationAsRead(Long userId, Long senderId);

    /**
     * Delete a message
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * Delete entire conversation
     */
    void deleteConversation(Long userId, Long otherUserId);

    /**
     * Count unread messages for a user
     */
    Long countUnreadMessages(Long userId);

    /**
     * Count unread messages in a specific conversation
     */
    Long countUnreadInConversation(Long userId, Long senderId);
}
