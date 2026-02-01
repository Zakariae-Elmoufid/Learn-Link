package org.example.learnlink.modules.matching.service;

import org.example.learnlink.modules.matching.dto.request.CreateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.request.UpdateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.response.GroupMemberResponse;
import org.example.learnlink.modules.matching.dto.response.StudyGroupResponse;
import org.example.learnlink.modules.matching.entity.enums.GroupRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for study group operations.
 */
public interface IStudyGroupService {

    // ==================== Group Management ====================

    /**
     * Create a new study group.
     *
     * @param ownerId ID of the user creating the group
     * @param dto     group creation data
     * @return the created group
     */
    StudyGroupResponse createGroup(Long ownerId, CreateStudyGroupDto dto);

    /**
     * Update an existing study group.
     *
     * @param groupId ID of the group
     * @param userId  ID of the user making the update (must be owner/admin)
     * @param dto     update data
     * @return the updated group
     */
    StudyGroupResponse updateGroup(Long groupId, Long userId, UpdateStudyGroupDto dto);

    /**
     * Delete/disband a study group.
     *
     * @param groupId ID of the group
     * @param userId  ID of the user (must be owner)
     */
    void deleteGroup(Long groupId, Long userId);

    /**
     * Get group details by ID.
     *
     * @param groupId ID of the group
     * @param userId  ID of the current user (for context enrichment)
     * @return group details
     */
    StudyGroupResponse getGroup(Long groupId, Long userId);

    /**
     * Get group details with all members.
     *
     * @param groupId ID of the group
     * @param userId  ID of the current user
     * @return group details with member list
     */
    StudyGroupResponse getGroupWithMembers(Long groupId, Long userId);

    // ==================== Discovery ====================

    /**
     * Get public groups for discovery.
     *
     * @param pageable pagination info
     * @return page of public groups
     */
    Page<StudyGroupResponse> discoverGroups(Pageable pageable);

    /**
     * Search groups by keyword.
     *
     * @param keyword  search keyword
     * @param pageable pagination info
     * @return page of matching groups
     */
    Page<StudyGroupResponse> searchGroups(String keyword, Pageable pageable);

    /**
     * Get groups by subject.
     *
     * @param subjectId ID of the subject
     * @return list of groups for that subject
     */
    List<StudyGroupResponse> getGroupsBySubject(Long subjectId);

    // ==================== User's Groups ====================

    /**
     * Get groups that a user is a member of.
     *
     * @param userId ID of the user
     * @return list of groups
     */
    List<StudyGroupResponse> getMyGroups(Long userId);

    /**
     * Get groups owned by a user.
     *
     * @param userId ID of the user
     * @return list of owned groups
     */
    List<StudyGroupResponse> getOwnedGroups(Long userId);

    // ==================== Membership Management ====================

    /**
     * Join a public group directly.
     *
     * @param groupId ID of the group
     * @param userId  ID of the user joining
     */
    void joinGroup(Long groupId, Long userId);

    /**
     * Request to join a private group.
     *
     * @param groupId ID of the group
     * @param userId  ID of the user requesting
     */
    void requestToJoin(Long groupId, Long userId);

    /**
     * Leave a group.
     *
     * @param groupId ID of the group
     * @param userId  ID of the user leaving
     */
    void leaveGroup(Long groupId, Long userId);

    /**
     * Get pending join requests for a group (admin only).
     *
     * @param groupId ID of the group
     * @param userId  ID of the requesting user (must be admin)
     * @return list of pending requests
     */
    List<GroupMemberResponse> getPendingRequests(Long groupId, Long userId);

    /**
     * Approve a join request.
     *
     * @param groupId     ID of the group
     * @param adminId     ID of the admin approving
     * @param requesterId ID of the user who requested
     */
    void approveJoinRequest(Long groupId, Long adminId, Long requesterId);

    /**
     * Reject a join request.
     *
     * @param groupId     ID of the group
     * @param adminId     ID of the admin rejecting
     * @param requesterId ID of the user who requested
     */
    void rejectJoinRequest(Long groupId, Long adminId, Long requesterId);

    /**
     * Remove a member from a group.
     *
     * @param groupId  ID of the group
     * @param adminId  ID of the admin removing
     * @param memberId ID of the member to remove
     */
    void removeMember(Long groupId, Long adminId, Long memberId);

    /**
     * Update a member's role.
     *
     * @param groupId  ID of the group
     * @param ownerId  ID of the owner (only owner can change roles)
     * @param memberId ID of the member
     * @param newRole  the new role
     */
    void updateMemberRole(Long groupId, Long ownerId, Long memberId, GroupRole newRole);

    /**
     * Get all members of a group.
     *
     * @param groupId ID of the group
     * @return list of members
     */
    List<GroupMemberResponse> getGroupMembers(Long groupId);
}
