package org.example.learnlink.modules.matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.matching.dto.request.CreateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.request.UpdateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.response.GroupMemberResponse;
import org.example.learnlink.modules.matching.dto.response.StudyGroupResponse;
import org.example.learnlink.modules.matching.entity.GroupMembership;
import org.example.learnlink.modules.matching.entity.StudyGroup;
import org.example.learnlink.modules.matching.entity.enums.GroupRole;
import org.example.learnlink.modules.matching.entity.enums.GroupStatus;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;
import org.example.learnlink.modules.matching.event.JoinRequestEvent;
import org.example.learnlink.modules.matching.event.MemberJoinedGroupEvent;
import org.example.learnlink.modules.matching.event.MemberLeftGroupEvent;
import org.example.learnlink.modules.matching.event.StudyGroupCreatedEvent;
import org.example.learnlink.modules.matching.mapper.StudyGroupMapper;
import org.example.learnlink.modules.matching.repository.GroupMembershipRepository;
import org.example.learnlink.modules.matching.repository.StudyGroupRepository;
import org.example.learnlink.modules.user.entity.UserProfile;
import org.example.learnlink.modules.user.repository.UserProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of study group service.
 * Handles group creation, membership, and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudyGroupServiceImpl implements IStudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudyGroupMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== Group Management ====================

    @Override
    public StudyGroupResponse createGroup(Long ownerId, CreateStudyGroupDto dto) {
        log.info("Creating study group '{}' for user {}", dto.getName(), ownerId);

        // Create the group
        StudyGroup group = mapper.toEntity(dto);
        group.setOwnerId(ownerId);
        group.setStatus(GroupStatus.ACTIVE);

        StudyGroup savedGroup = studyGroupRepository.save(group);

        // Add owner as first member with OWNER role
        GroupMembership ownerMembership = GroupMembership.builder()
                .studyGroup(savedGroup)
                .userId(ownerId)
                .role(GroupRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(ownerMembership);

        // Publish event
        eventPublisher.publishEvent(StudyGroupCreatedEvent.builder()
                .groupId(savedGroup.getId())
                .groupName(savedGroup.getName())
                .ownerId(ownerId)
                .subjectId(savedGroup.getSubjectId())
                .build());

        log.info("Created study group {} with owner {}", savedGroup.getId(), ownerId);

        return enrichResponse(mapper.toResponse(savedGroup), ownerId);
    }

    @Override
    public StudyGroupResponse updateGroup(Long groupId, Long userId, UpdateStudyGroupDto dto) {
        log.info("Updating study group {} by user {}", groupId, userId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateAdminAccess(group, userId);

        // Update fields if provided
        if (dto.getName() != null) {
            group.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            group.setDescription(dto.getDescription());
        }
        if (dto.getMaxMembers() != null) {
            if (dto.getMaxMembers() < group.getActiveMemberCount()) {
                throw new IllegalArgumentException("Cannot set max members below current member count");
            }
            group.setMaxMembers(dto.getMaxMembers());
        }
        if (dto.getIsPublic() != null) {
            group.setIsPublic(dto.getIsPublic());
        }
        if (dto.getCoverImageUrl() != null) {
            group.setCoverImageUrl(dto.getCoverImageUrl());
        }

        StudyGroup updated = studyGroupRepository.save(group);
        log.info("Updated study group {}", groupId);

        return enrichResponse(mapper.toResponse(updated), userId);
    }

    @Override
    public void deleteGroup(Long groupId, Long userId) {
        log.info("Deleting study group {} by user {}", groupId, userId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateOwnerAccess(group, userId);

        group.setStatus(GroupStatus.DISBANDED);
        studyGroupRepository.save(group);

        log.info("Disbanded study group {}", groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyGroupResponse getGroup(Long groupId, Long userId) {
        StudyGroup group = findGroupOrThrow(groupId);
        return enrichResponse(mapper.toResponse(group), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyGroupResponse getGroupWithMembers(Long groupId, Long userId) {
        StudyGroup group = studyGroupRepository.findByIdWithMemberships(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup", "id", groupId));

        StudyGroupResponse response = enrichResponse(mapper.toResponse(group), userId);

        // Add members
        List<GroupMemberResponse> members = getGroupMembers(groupId);
        response.setMembers(members);

        return response;
    }

    // ==================== Discovery ====================

    @Override
    @Transactional(readOnly = true)
    public Page<StudyGroupResponse> discoverGroups(Pageable pageable) {
        return studyGroupRepository
                .findPublicGroups(GroupStatus.ACTIVE, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyGroupResponse> searchGroups(String keyword, Pageable pageable) {
        return studyGroupRepository
                .searchByKeyword(keyword, GroupStatus.ACTIVE, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getGroupsBySubject(Long subjectId) {
        return studyGroupRepository
                .findBySubjectIdAndStatus(subjectId, GroupStatus.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== User's Groups ====================

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getMyGroups(Long userId) {
        return studyGroupRepository
                .findGroupsByMembership(userId)
                .stream()
                .map(g -> enrichResponse(mapper.toResponse(g), userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getOwnedGroups(Long userId) {
        return studyGroupRepository
                .findByOwnerIdAndStatusNot(userId, GroupStatus.DISBANDED)
                .stream()
                .map(g -> enrichResponse(mapper.toResponse(g), userId))
                .collect(Collectors.toList());
    }

    // ==================== Membership Management ====================

    @Override
    public void joinGroup(Long groupId, Long userId) {
        log.info("User {} joining group {}", userId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);

        // Validate group is public
        if (!group.getIsPublic()) {
            throw new IllegalStateException("Cannot join private group directly. Use requestToJoin instead.");
        }

        // Validate group can accept members
        if (!group.canAcceptMembers()) {
            throw new IllegalStateException("Group is full or not accepting new members");
        }

        // Check if already a member
        if (membershipRepository.existsActiveOrPendingMembership(groupId, userId)) {
            throw new IllegalStateException("You are already a member or have a pending request");
        }

        // Create membership
        GroupMembership membership = GroupMembership.builder()
                .studyGroup(group)
                .userId(userId)
                .role(GroupRole.MEMBER)
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(membership);

        // Update group status if full
        updateGroupStatusIfFull(group);

        // Publish event
        eventPublisher.publishEvent(MemberJoinedGroupEvent.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .userId(userId)
                .ownerId(group.getOwnerId())
                .directJoin(true)
                .build());

        log.info("User {} joined group {}", userId, groupId);
    }

    @Override
    public void requestToJoin(Long groupId, Long userId) {
        log.info("User {} requesting to join group {}", userId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);

        // Validate group can accept members
        if (!group.canAcceptMembers()) {
            throw new IllegalStateException("Group is full or not accepting new members");
        }

        // Check if already a member
        if (membershipRepository.existsActiveOrPendingMembership(groupId, userId)) {
            throw new IllegalStateException("You are already a member or have a pending request");
        }

        // Create pending membership
        GroupMembership membership = GroupMembership.builder()
                .studyGroup(group)
                .userId(userId)
                .role(GroupRole.MEMBER)
                .status(MembershipStatus.PENDING)
                .build();
        membershipRepository.save(membership);

        // Publish event for notification
        eventPublisher.publishEvent(JoinRequestEvent.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .requesterId(userId)
                .ownerId(group.getOwnerId())
                .build());

        log.info("User {} requested to join group {}", userId, groupId);
    }

    @Override
    public void leaveGroup(Long groupId, Long userId) {
        log.info("User {} leaving group {}", userId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);

        // Owner cannot leave (must transfer ownership or disband)
        if (group.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Owner cannot leave the group. Transfer ownership or disband instead.");
        }

        GroupMembership membership = membershipRepository.findActiveMembership(groupId, userId)
                .orElseThrow(() -> new IllegalStateException("You are not a member of this group"));

        membership.setStatus(MembershipStatus.LEFT);
        membership.setLeftAt(LocalDateTime.now());
        membershipRepository.save(membership);

        // Update group status if it was full
        if (group.getStatus() == GroupStatus.FULL) {
            group.setStatus(GroupStatus.ACTIVE);
            studyGroupRepository.save(group);
        }

        // Publish event
        eventPublisher.publishEvent(MemberLeftGroupEvent.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .userId(userId)
                .ownerId(group.getOwnerId())
                .voluntary(true)
                .build());

        log.info("User {} left group {}", userId, groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getPendingRequests(Long groupId, Long userId) {
        StudyGroup group = findGroupOrThrow(groupId);
        validateAdminAccess(group, userId);

        List<GroupMembership> pending = membershipRepository
                .findByStudyGroupIdAndStatusOrderByJoinedAtDesc(groupId, MembershipStatus.PENDING);

        return enrichMemberResponses(pending);
    }

    @Override
    public void approveJoinRequest(Long groupId, Long adminId, Long requesterId) {
        log.info("Admin {} approving join request from {} for group {}", adminId, requesterId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateAdminAccess(group, adminId);

        // Validate group can accept members
        if (!group.canAcceptMembers()) {
            throw new IllegalStateException("Group is full");
        }

        GroupMembership membership = membershipRepository.findByStudyGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request", "userId", requesterId));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        membership.setStatus(MembershipStatus.ACTIVE);
        membershipRepository.save(membership);

        // Update group status if full
        updateGroupStatusIfFull(group);

        // Publish event
        eventPublisher.publishEvent(MemberJoinedGroupEvent.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .userId(requesterId)
                .ownerId(group.getOwnerId())
                .directJoin(false)
                .build());

        log.info("Approved join request from {} for group {}", requesterId, groupId);
    }

    @Override
    public void rejectJoinRequest(Long groupId, Long adminId, Long requesterId) {
        log.info("Admin {} rejecting join request from {} for group {}", adminId, requesterId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateAdminAccess(group, adminId);

        GroupMembership membership = membershipRepository.findByStudyGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request", "userId", requesterId));

        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        membership.setStatus(MembershipStatus.REMOVED);
        membership.setLeftAt(LocalDateTime.now());
        membershipRepository.save(membership);

        log.info("Rejected join request from {} for group {}", requesterId, groupId);
    }

    @Override
    public void removeMember(Long groupId, Long adminId, Long memberId) {
        log.info("Admin {} removing member {} from group {}", adminId, memberId, groupId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateAdminAccess(group, adminId);

        // Cannot remove owner
        if (group.getOwnerId().equals(memberId)) {
            throw new IllegalStateException("Cannot remove the group owner");
        }

        // Admin cannot remove another admin (only owner can)
        GroupMembership memberToRemove = membershipRepository.findActiveMembership(groupId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", memberId));

        if (memberToRemove.getRole() == GroupRole.ADMIN && !group.getOwnerId().equals(adminId)) {
            throw new IllegalStateException("Only the owner can remove an admin");
        }

        memberToRemove.setStatus(MembershipStatus.REMOVED);
        memberToRemove.setLeftAt(LocalDateTime.now());
        membershipRepository.save(memberToRemove);

        // Update group status if it was full
        if (group.getStatus() == GroupStatus.FULL) {
            group.setStatus(GroupStatus.ACTIVE);
            studyGroupRepository.save(group);
        }

        // Publish event
        eventPublisher.publishEvent(MemberLeftGroupEvent.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .userId(memberId)
                .ownerId(group.getOwnerId())
                .voluntary(false)
                .removedByUserId(adminId)
                .build());

        log.info("Removed member {} from group {}", memberId, groupId);
    }

    @Override
    public void updateMemberRole(Long groupId, Long ownerId, Long memberId, GroupRole newRole) {
        log.info("Owner {} updating role of member {} to {} in group {}", ownerId, memberId, newRole, groupId);

        StudyGroup group = findGroupOrThrow(groupId);
        validateOwnerAccess(group, ownerId);

        // Cannot change own role
        if (ownerId.equals(memberId)) {
            throw new IllegalStateException("Cannot change your own role");
        }

        // Cannot assign OWNER role (must transfer ownership separately)
        if (newRole == GroupRole.OWNER) {
            throw new IllegalStateException("Use transfer ownership to change group owner");
        }

        GroupMembership membership = membershipRepository.findActiveMembership(groupId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", memberId));

        membership.setRole(newRole);
        membershipRepository.save(membership);

        log.info("Updated role of member {} to {} in group {}", memberId, newRole, groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        List<GroupMembership> members = membershipRepository
                .findByStudyGroupIdAndStatus(groupId, MembershipStatus.ACTIVE);

        return enrichMemberResponses(members);
    }

    // ==================== Private Helper Methods ====================

    private StudyGroup findGroupOrThrow(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup", "id", groupId));
    }

    private void validateAdminAccess(StudyGroup group, Long userId) {
        if (group.getOwnerId().equals(userId)) {
            return; // Owner has admin access
        }

        Optional<GroupMembership> membership = membershipRepository.findActiveMembership(group.getId(), userId);
        if (membership.isEmpty() || !membership.get().hasAdminPrivileges()) {
            throw new IllegalStateException("You do not have admin privileges for this group");
        }
    }

    private void validateOwnerAccess(StudyGroup group, Long userId) {
        if (!group.getOwnerId().equals(userId)) {
            throw new IllegalStateException("Only the group owner can perform this action");
        }
    }

    private void updateGroupStatusIfFull(StudyGroup group) {
        if (group.isFull() && group.getStatus() == GroupStatus.ACTIVE) {
            group.setStatus(GroupStatus.FULL);
            studyGroupRepository.save(group);
        }
    }

    private StudyGroupResponse enrichResponse(StudyGroupResponse response, Long userId) {
        // Add owner name
        userProfileRepository.findByUserId(response.getOwnerId())
                .ifPresent(profile -> mapper.enrichWithOwner(response, profile));

        // Add user context
        Optional<GroupMembership> membership = membershipRepository
                .findByStudyGroupIdAndUserId(response.getId(), userId);

        boolean isMember = membership.map(m -> m.getStatus() == MembershipStatus.ACTIVE).orElse(false);
        boolean isAdmin = membership.map(GroupMembership::hasAdminPrivileges).orElse(false);
        boolean hasPending = membership.map(m -> m.getStatus() == MembershipStatus.PENDING).orElse(false);

        mapper.enrichWithContext(response, userId, isMember, isAdmin, hasPending);

        return response;
    }

    private List<GroupMemberResponse> enrichMemberResponses(List<GroupMembership> memberships) {
        if (memberships.isEmpty()) {
            return List.of();
        }

        // Batch load user profiles
        List<Long> userIds = memberships.stream()
                .map(GroupMembership::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p));

        return memberships.stream()
                .map(m -> mapper.toMemberResponseWithProfile(m, profileMap.get(m.getUserId())))
                .collect(Collectors.toList());
    }
}
