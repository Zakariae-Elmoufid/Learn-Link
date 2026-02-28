package org.example.learnlink.modules.admin.repository;

import org.example.learnlink.modules.admin.entity.ModerationActionType;
import org.example.learnlink.modules.admin.entity.ModerationLog;
import org.example.learnlink.modules.admin.entity.ModerationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for moderation logs
 */
@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {

    /**
     * Find all logs ordered by creation date
     */
    Page<ModerationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find logs by moderator ID
     */
    Page<ModerationLog> findByModeratorIdOrderByCreatedAtDesc(Long moderatorId, Pageable pageable);

    /**
     * Find logs by action type
     */
    Page<ModerationLog> findByActionTypeOrderByCreatedAtDesc(ModerationActionType actionType, Pageable pageable);

    /**
     * Find logs by target type
     */
    Page<ModerationLog> findByTargetTypeOrderByCreatedAtDesc(ModerationTargetType targetType, Pageable pageable);

    /**
     * Find logs for a specific target
     */
    List<ModerationLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(ModerationTargetType targetType, Long targetId);

    /**
     * Find logs by target user (content owner)
     */
    Page<ModerationLog> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId, Pageable pageable);

    /**
     * Find logs within a date range
     */
    @Query("SELECT m FROM ModerationLog m WHERE m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt DESC")
    Page<ModerationLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count actions by type within a date range
     */
    @Query("SELECT COUNT(m) FROM ModerationLog m WHERE m.actionType = :actionType AND m.createdAt >= :since")
    long countByActionTypeSince(@Param("actionType") ModerationActionType actionType, @Param("since") LocalDateTime since);

    /**
     * Count actions by moderator
     */
    long countByModeratorId(Long moderatorId);
}
