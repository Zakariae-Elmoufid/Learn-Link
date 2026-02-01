package org.example.learnlink.modules.matching.repository;

import org.example.learnlink.modules.matching.entity.GroupMembership;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupMembership entity operations.
 */
@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    /**
     * Find membership by group and user
     */
    Optional<GroupMembership> findByStudyGroupIdAndUserId(Long groupId, Long userId);

    /**
     * Find active membership by group and user
     */
    @Query("SELECT m FROM GroupMembership m WHERE m.studyGroup.id = :groupId " +
           "AND m.userId = :userId AND m.status = 'ACTIVE'")
    Optional<GroupMembership> findActiveMembership(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * Find all active members of a group
     */
    List<GroupMembership> findByStudyGroupIdAndStatus(Long groupId, MembershipStatus status);

    /**
     * Find all memberships of a user
     */
    List<GroupMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    /**
     * Find pending join requests for a group
     */
    List<GroupMembership> findByStudyGroupIdAndStatusOrderByJoinedAtDesc(Long groupId, MembershipStatus status);

    /**
     * Check if user is already a member (any status except LEFT/REMOVED)
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM GroupMembership m " +
           "WHERE m.studyGroup.id = :groupId AND m.userId = :userId AND m.status IN ('ACTIVE', 'PENDING')")
    boolean existsActiveOrPendingMembership(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * Count active members in a group
     */
    long countByStudyGroupIdAndStatus(Long groupId, MembershipStatus status);

    /**
     * Find all user IDs that are active members of a group
     */
    @Query("SELECT m.userId FROM GroupMembership m WHERE m.studyGroup.id = :groupId AND m.status = 'ACTIVE'")
    List<Long> findActiveMemberUserIds(@Param("groupId") Long groupId);
}
