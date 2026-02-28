package org.example.learnlink.modules.admin.repository;

import org.example.learnlink.modules.admin.entity.ModeratorPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for moderator permissions management
 */
@Repository
public interface ModeratorPermissionRepository extends JpaRepository<ModeratorPermissionEntity, Long> {

    /**
     * Find permissions by user ID
     */
    Optional<ModeratorPermissionEntity> findByUserId(Long userId);

    /**
     * Check if user has moderator permissions
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete permissions by user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Find all moderator permissions with ordering
     */
    @Query("SELECT m FROM ModeratorPermissionEntity m ORDER BY m.assignedAt DESC")
    List<ModeratorPermissionEntity> findAllOrderByAssignedAtDesc();

    /**
     * Find moderators assigned by a specific admin
     */
    List<ModeratorPermissionEntity> findByAssignedBy(Long adminId);

    /**
     * Count total moderators
     */
    @Query("SELECT COUNT(m) FROM ModeratorPermissionEntity m")
    long countModerators();
}
