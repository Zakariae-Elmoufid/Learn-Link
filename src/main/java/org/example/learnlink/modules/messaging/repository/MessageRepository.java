package org.example.learnlink.modules.messaging.repository;

import org.example.learnlink.modules.messaging.entity.Message;
import org.example.learnlink.modules.messaging.entity.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find conversation between two users ordered by creation time
     */
    @Query("""
        SELECT m FROM Message m 
        WHERE (m.senderId = :userId1 AND m.recipientId = :userId2)
           OR (m.senderId = :userId2 AND m.recipientId = :userId1)
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable);

    /**
     * Find unread messages for a user
     */
    @Query("SELECT m FROM Message m WHERE m.recipientId = :userId AND m.status = 'SENT' ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);

    /**
     * Find unread messages from a specific sender
     */
    @Query("SELECT m FROM Message m WHERE m.recipientId = :recipientId AND m.senderId = :senderId AND m.status = 'SENT'")
    List<Message> findUnreadMessagesFromSender(
            @Param("recipientId") Long recipientId,
            @Param("senderId") Long senderId);

    /**
     * Count unread messages for a user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = :userId AND m.status = 'SENT'")
    Long countUnreadMessages(@Param("userId") Long userId);

    /**
     * Count unread messages in a conversation
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = :recipientId AND m.senderId = :senderId AND m.status = 'SENT'")
    Long countUnreadMessagesFromSender(
            @Param("recipientId") Long recipientId,
            @Param("senderId") Long senderId);

    /**
     * Mark all messages from sender as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :status, m.readAt = CURRENT_TIMESTAMP WHERE m.recipientId = :recipientId AND m.senderId = :senderId AND m.status = 'SENT'")
    int markMessagesAsRead(
            @Param("recipientId") Long recipientId,
            @Param("senderId") Long senderId,
            @Param("status") MessageStatus status);

    /**
     * Find all conversations for a user (distinct conversation partners)
     */
    @Query("""
        SELECT DISTINCT CASE 
            WHEN m.senderId = :userId THEN m.recipientId 
            ELSE m.senderId 
        END FROM Message m 
        WHERE m.senderId = :userId OR m.recipientId = :userId
        """)
    List<Long> findConversationPartners(@Param("userId") Long userId);

    /**
     * Find the last message in a conversation
     */
    @Query("""
        SELECT m FROM Message m 
        WHERE (m.senderId = :userId1 AND m.recipientId = :userId2)
           OR (m.senderId = :userId2 AND m.recipientId = :userId1)
        ORDER BY m.createdAt DESC
        LIMIT 1
        """)
    Message findLastMessageInConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    /**
     * Find messages by sender
     */
    Page<Message> findBySenderId(Long senderId, Pageable pageable);

    /**
     * Find messages by recipient
     */
    Page<Message> findByRecipientId(Long recipientId, Pageable pageable);

    /**
     * Delete all messages in a conversation
     */
    @Modifying
    @Query("""
        DELETE FROM Message m 
        WHERE (m.senderId = :userId1 AND m.recipientId = :userId2)
           OR (m.senderId = :userId2 AND m.recipientId = :userId1)
        """)
    int deleteConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}
