package org.example.learnlink.modules.matching.repository;

import org.example.learnlink.modules.matching.entity.StudyGroup;
import org.example.learnlink.modules.matching.entity.enums.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudyGroup entity operations.
 */
@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    /**
     * Find groups by owner ID
     */
    List<StudyGroup> findByOwnerIdAndStatusNot(Long ownerId, GroupStatus excludeStatus);

    /**
     * Find groups by subject ID
     */
    List<StudyGroup> findBySubjectIdAndStatus(Long subjectId, GroupStatus status);

    /**
     * Find public active groups (for discovery)
     */
    @Query("SELECT g FROM StudyGroup g WHERE g.isPublic = true AND g.status = :status ORDER BY g.createdAt DESC")
    Page<StudyGroup> findPublicGroups(@Param("status") GroupStatus status, Pageable pageable);

    /**
     * Search groups by name or description
     */
    @Query("SELECT g FROM StudyGroup g WHERE g.status = :status AND g.isPublic = true " +
           "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StudyGroup> searchByKeyword(@Param("keyword") String keyword, @Param("status") GroupStatus status, Pageable pageable);

    /**
     * Find groups a user is a member of (active memberships)
     */
    @Query("SELECT g FROM StudyGroup g JOIN g.memberships m " +
           "WHERE m.userId = :userId AND m.status = 'ACTIVE' AND g.status != 'DISBANDED'")
    List<StudyGroup> findGroupsByMembership(@Param("userId") Long userId);

    /**
     * Count groups owned by a user
     */
    long countByOwnerIdAndStatusNot(Long ownerId, GroupStatus excludeStatus);

    /**
     * Find group with memberships loaded
     */
    @Query("SELECT g FROM StudyGroup g LEFT JOIN FETCH g.memberships WHERE g.id = :groupId")
    Optional<StudyGroup> findByIdWithMemberships(@Param("groupId") Long groupId);
    
    // Admin statistics queries
    
    /**
     * Count groups by status
     */
    long countByStatus(GroupStatus status);
}
