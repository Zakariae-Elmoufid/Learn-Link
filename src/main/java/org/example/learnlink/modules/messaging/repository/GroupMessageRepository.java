package org.example.learnlink.modules.messaging.repository;

import org.example.learnlink.modules.messaging.entity.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupMessage entity operations.
 */
@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {

    /**
     * Find all messages for a group, ordered by creation time descending (newest first).
     * Paginated for performance.
     */
    Page<GroupMessage> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * Find messages created after a specific timestamp (for real-time sync).
     */
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.groupId = :groupId AND gm.createdAt > :since ORDER BY gm.createdAt ASC")
    List<GroupMessage> findByGroupIdAndCreatedAtAfter(
            @Param("groupId") Long groupId,
            @Param("since") LocalDateTime since
    );

    /**
     * Count unread messages for a user in a group.
     */
    @Query("""
        SELECT COUNT(gm) FROM GroupMessage gm 
        WHERE gm.groupId = :groupId 
        AND gm.senderId != :userId 
        AND NOT EXISTS (
            SELECT rs FROM GroupMessageReadStatus rs 
            WHERE rs.groupMessage = gm AND rs.userId = :userId
        )
    """)
    Long countUnreadMessages(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId
    );

    /**
     * Find the last message in a group.
     */
    Optional<GroupMessage> findFirstByGroupIdOrderByCreatedAtDesc(Long groupId);

    /**
     * Delete all messages in a group (for group deletion).
     */
    void deleteByGroupId(Long groupId);

    /**
     * Count total messages in a group
     */
    Long countByGroupId(Long groupId);
}
