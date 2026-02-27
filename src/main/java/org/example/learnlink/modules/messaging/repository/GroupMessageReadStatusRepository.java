package org.example.learnlink.modules.messaging.repository;

import org.example.learnlink.modules.messaging.entity.GroupMessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupMessageReadStatus entity operations.
 */
@Repository
public interface GroupMessageReadStatusRepository extends JpaRepository<GroupMessageReadStatus, Long> {

    /**
     * Check if a user has read a specific message.
     */
    boolean existsByGroupMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Find read status for a specific message and user.
     */
    Optional<GroupMessageReadStatus> findByGroupMessageIdAndUserId(Long messageId, Long userId);

    /**
     * Get all read statuses for a message (to show who has read it).
     */
    List<GroupMessageReadStatus> findByGroupMessageId(Long messageId);

    /**
     * Count how many users have read a message.
     */
    Long countByGroupMessageId(Long messageId);

    /**
     * Mark all unread messages in a group as read for a user.
     * Uses native query for bulk update efficiency.
     */
    @Modifying
    @Query(value = """
        INSERT INTO group_message_read_status (message_id, user_id, read_at)
        SELECT gm.id, :userId, CURRENT_TIMESTAMP
        FROM group_messages gm
        WHERE gm.group_id = :groupId
        AND gm.sender_id != :userId
        AND NOT EXISTS (
            SELECT 1 FROM group_message_read_status rs 
            WHERE rs.message_id = gm.id AND rs.user_id = :userId
        )
    """, nativeQuery = true)
    int markAllAsReadForUser(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
