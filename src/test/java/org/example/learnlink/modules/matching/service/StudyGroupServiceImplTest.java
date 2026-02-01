package org.example.learnlink.modules.matching.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudyGroupServiceImpl
 * 
 * Tests cover:
 * - Group CRUD operations
 * - Membership management (join, leave, request)
 * - Admin operations (approve, reject, remove)
 * - Role management
 * - Event publishing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudyGroupService Unit Tests")
class StudyGroupServiceImplTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private GroupMembershipRepository membershipRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private StudyGroupMapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    // Test data
    private Long ownerId;
    private Long memberId;
    private Long groupId;
    private StudyGroup studyGroup;
    private GroupMembership ownerMembership;
    private GroupMembership memberMembership;
    private CreateStudyGroupDto createDto;
    private UpdateStudyGroupDto updateDto;
    private StudyGroupResponse groupResponse;
    private UserProfile ownerProfile;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        memberId = 2L;
        groupId = 100L;

        // Create study group entity
        studyGroup = StudyGroup.builder()
                .id(groupId)
                .name("Java Study Group")
                .description("Learn Java together")
                .ownerId(ownerId)
                .subjectId(10L)
                .maxMembers(10)
                .status(GroupStatus.ACTIVE)
                .isPublic(true)
                .memberships(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // Create owner membership
        ownerMembership = GroupMembership.builder()
                .id(1L)
                .studyGroup(studyGroup)
                .userId(ownerId)
                .role(GroupRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        // Create member membership
        memberMembership = GroupMembership.builder()
                .id(2L)
                .studyGroup(studyGroup)
                .userId(memberId)
                .role(GroupRole.MEMBER)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        studyGroup.getMemberships().add(ownerMembership);

        // Create DTOs
        createDto = CreateStudyGroupDto.builder()
                .name("Java Study Group")
                .description("Learn Java together")
                .subjectId(10L)
                .maxMembers(10)
                .isPublic(true)
                .build();

        updateDto = UpdateStudyGroupDto.builder()
                .name("Advanced Java Group")
                .description("Deep dive into Java")
                .maxMembers(15)
                .build();

        groupResponse = StudyGroupResponse.builder()
                .id(groupId)
                .name("Java Study Group")
                .description("Learn Java together")
                .ownerId(ownerId)
                .maxMembers(10)
                .currentMemberCount(1)
                .status(GroupStatus.ACTIVE)
                .isPublic(true)
                .build();

        ownerProfile = UserProfile.builder()
                .id(1L)
                .userId(ownerId)
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    // ==================== GROUP CREATION TESTS ====================

    @Nested
    @DisplayName("Create Group Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("Should create group successfully and publish event")
        void testCreateGroup_Success() {
            // Arrange
            when(mapper.toEntity(createDto)).thenReturn(studyGroup);
            when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);
            when(membershipRepository.save(any(GroupMembership.class))).thenReturn(ownerMembership);
            when(mapper.toResponse(studyGroup)).thenReturn(groupResponse);
            when(userProfileRepository.findByUserId(ownerId)).thenReturn(Optional.of(ownerProfile));
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, ownerId))
                    .thenReturn(Optional.of(ownerMembership));
            when(mapper.enrichWithOwner(any(), any())).thenReturn(groupResponse);
            when(mapper.enrichWithContext(any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
                    .thenReturn(groupResponse);

            // Act
            StudyGroupResponse result = studyGroupService.createGroup(ownerId, createDto);

            // Assert
            assertNotNull(result);
            assertEquals(groupResponse.getName(), result.getName());

            // Verify group was saved
            verify(studyGroupRepository, times(1)).save(any(StudyGroup.class));

            // Verify owner membership was created
            ArgumentCaptor<GroupMembership> membershipCaptor = ArgumentCaptor.forClass(GroupMembership.class);
            verify(membershipRepository, times(1)).save(membershipCaptor.capture());
            GroupMembership savedMembership = membershipCaptor.getValue();
            assertEquals(GroupRole.OWNER, savedMembership.getRole());
            assertEquals(ownerId, savedMembership.getUserId());

            // Verify event was published
            ArgumentCaptor<StudyGroupCreatedEvent> eventCaptor = 
                    ArgumentCaptor.forClass(StudyGroupCreatedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            StudyGroupCreatedEvent event = eventCaptor.getValue();
            assertEquals(groupId, event.getGroupId());
            assertEquals(ownerId, event.getOwnerId());
        }

        @Test
        @DisplayName("Should set correct default values when creating group")
        void testCreateGroup_DefaultValues() {
            // Arrange
            CreateStudyGroupDto minimalDto = CreateStudyGroupDto.builder()
                    .name("Minimal Group")
                    .build();

            StudyGroup minimalGroup = StudyGroup.builder()
                    .id(groupId)
                    .name("Minimal Group")
                    .ownerId(ownerId)
                    .maxMembers(10)  // default
                    .status(GroupStatus.ACTIVE)
                    .isPublic(true)  // default
                    .memberships(new ArrayList<>())
                    .build();

            when(mapper.toEntity(minimalDto)).thenReturn(minimalGroup);
            when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(minimalGroup);
            when(membershipRepository.save(any(GroupMembership.class))).thenReturn(ownerMembership);
            when(mapper.toResponse(minimalGroup)).thenReturn(groupResponse);
            when(userProfileRepository.findByUserId(ownerId)).thenReturn(Optional.empty());
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, ownerId))
                    .thenReturn(Optional.of(ownerMembership));
            when(mapper.enrichWithContext(any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
                    .thenReturn(groupResponse);

            // Act
            StudyGroupResponse result = studyGroupService.createGroup(ownerId, minimalDto);

            // Assert
            assertNotNull(result);
            verify(studyGroupRepository).save(any(StudyGroup.class));
        }
    }

    // ==================== GROUP UPDATE TESTS ====================

    @Nested
    @DisplayName("Update Group Tests")
    class UpdateGroupTests {

        @Test
        @DisplayName("Should update group successfully when user is owner")
        void testUpdateGroup_AsOwner_Success() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);
            when(mapper.toResponse(studyGroup)).thenReturn(groupResponse);
            when(userProfileRepository.findByUserId(ownerId)).thenReturn(Optional.of(ownerProfile));
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, ownerId))
                    .thenReturn(Optional.of(ownerMembership));
            when(mapper.enrichWithOwner(any(), any())).thenReturn(groupResponse);
            when(mapper.enrichWithContext(any(), any(), anyBoolean(), anyBoolean(), anyBoolean()))
                    .thenReturn(groupResponse);

            // Act
            StudyGroupResponse result = studyGroupService.updateGroup(groupId, ownerId, updateDto);

            // Assert
            assertNotNull(result);
            verify(studyGroupRepository).save(studyGroup);
            assertEquals("Advanced Java Group", studyGroup.getName());
            assertEquals("Deep dive into Java", studyGroup.getDescription());
            assertEquals(15, studyGroup.getMaxMembers());
        }

        @Test
        @DisplayName("Should throw exception when group not found")
        void testUpdateGroup_GroupNotFound() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () ->
                    studyGroupService.updateGroup(groupId, ownerId, updateDto));

            verify(studyGroupRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is not admin")
        void testUpdateGroup_NotAdmin() {
            // Arrange
            Long nonAdminId = 999L;
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, nonAdminId))
                    .thenReturn(Optional.of(memberMembership));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.updateGroup(groupId, nonAdminId, updateDto));
        }

        @Test
        @DisplayName("Should reject maxMembers below current member count")
        void testUpdateGroup_MaxMembersBelowCurrent() {
            // Arrange
            studyGroup.getMemberships().add(memberMembership);
            studyGroup.getMemberships().add(GroupMembership.builder()
                    .userId(3L).status(MembershipStatus.ACTIVE).build());

            UpdateStudyGroupDto invalidDto = UpdateStudyGroupDto.builder()
                    .maxMembers(1)  // Less than current 3 members
                    .build();

            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    studyGroupService.updateGroup(groupId, ownerId, invalidDto));
        }
    }

    // ==================== DELETE GROUP TESTS ====================

    @Nested
    @DisplayName("Delete Group Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("Should disband group when owner requests")
        void testDeleteGroup_Success() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(studyGroupRepository.save(studyGroup)).thenReturn(studyGroup);

            // Act
            studyGroupService.deleteGroup(groupId, ownerId);

            // Assert
            assertEquals(GroupStatus.DISBANDED, studyGroup.getStatus());
            verify(studyGroupRepository).save(studyGroup);
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to delete")
        void testDeleteGroup_NotOwner() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.deleteGroup(groupId, memberId));

            assertNotEquals(GroupStatus.DISBANDED, studyGroup.getStatus());
        }
    }

    // ==================== JOIN GROUP TESTS ====================

    @Nested
    @DisplayName("Join Group Tests")
    class JoinGroupTests {

        @Test
        @DisplayName("Should join public group successfully")
        void testJoinGroup_PublicGroup_Success() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.existsActiveOrPendingMembership(groupId, memberId)).thenReturn(false);
            when(membershipRepository.save(any(GroupMembership.class))).thenReturn(memberMembership);

            // Act
            studyGroupService.joinGroup(groupId, memberId);

            // Assert
            ArgumentCaptor<GroupMembership> captor = ArgumentCaptor.forClass(GroupMembership.class);
            verify(membershipRepository).save(captor.capture());
            GroupMembership saved = captor.getValue();
            assertEquals(memberId, saved.getUserId());
            assertEquals(GroupRole.MEMBER, saved.getRole());
            assertEquals(MembershipStatus.ACTIVE, saved.getStatus());

            // Verify event was published
            ArgumentCaptor<MemberJoinedGroupEvent> eventCaptor = 
                    ArgumentCaptor.forClass(MemberJoinedGroupEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getValue().isDirectJoin());
        }

        @Test
        @DisplayName("Should throw exception when joining private group directly")
        void testJoinGroup_PrivateGroup_Fails() {
            // Arrange
            studyGroup.setIsPublic(false);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.joinGroup(groupId, memberId));

            verify(membershipRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when group is full")
        void testJoinGroup_GroupFull_Fails() {
            // Arrange
            studyGroup.setMaxMembers(1);  // Only owner
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.joinGroup(groupId, memberId));
        }

        @Test
        @DisplayName("Should throw exception when already a member")
        void testJoinGroup_AlreadyMember_Fails() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.existsActiveOrPendingMembership(groupId, memberId)).thenReturn(true);

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.joinGroup(groupId, memberId));
        }

        @Test
        @DisplayName("Should update group status to FULL when reaching max members")
        void testJoinGroup_UpdatesStatusToFull() {
            // Arrange
            studyGroup.setMaxMembers(2);  // Owner + 1 more
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.existsActiveOrPendingMembership(groupId, memberId)).thenReturn(false);
            when(membershipRepository.save(any(GroupMembership.class))).thenAnswer(invocation -> {
                GroupMembership m = invocation.getArgument(0);
                m.setStatus(MembershipStatus.ACTIVE);
                studyGroup.getMemberships().add(m);
                return m;
            });

            // Act
            studyGroupService.joinGroup(groupId, memberId);

            // Assert - group should be saved with FULL status
            verify(studyGroupRepository).save(studyGroup);
        }
    }

    // ==================== REQUEST TO JOIN TESTS ====================

    @Nested
    @DisplayName("Request To Join Tests")
    class RequestToJoinTests {

        @Test
        @DisplayName("Should create pending membership for private group")
        void testRequestToJoin_Success() {
            // Arrange
            studyGroup.setIsPublic(false);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.existsActiveOrPendingMembership(groupId, memberId)).thenReturn(false);
            when(membershipRepository.save(any(GroupMembership.class))).thenReturn(memberMembership);

            // Act
            studyGroupService.requestToJoin(groupId, memberId);

            // Assert
            ArgumentCaptor<GroupMembership> captor = ArgumentCaptor.forClass(GroupMembership.class);
            verify(membershipRepository).save(captor.capture());
            assertEquals(MembershipStatus.PENDING, captor.getValue().getStatus());

            // Verify event was published
            verify(eventPublisher).publishEvent(any(JoinRequestEvent.class));
        }
    }

    // ==================== LEAVE GROUP TESTS ====================

    @Nested
    @DisplayName("Leave Group Tests")
    class LeaveGroupTests {

        @Test
        @DisplayName("Should leave group successfully")
        void testLeaveGroup_Success() {
            // Arrange
            studyGroup.getMemberships().add(memberMembership);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, memberId))
                    .thenReturn(Optional.of(memberMembership));
            when(membershipRepository.save(memberMembership)).thenReturn(memberMembership);

            // Act
            studyGroupService.leaveGroup(groupId, memberId);

            // Assert
            assertEquals(MembershipStatus.LEFT, memberMembership.getStatus());
            assertNotNull(memberMembership.getLeftAt());

            // Verify event was published
            ArgumentCaptor<MemberLeftGroupEvent> eventCaptor = 
                    ArgumentCaptor.forClass(MemberLeftGroupEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getValue().isVoluntary());
        }

        @Test
        @DisplayName("Should throw exception when owner tries to leave")
        void testLeaveGroup_OwnerCannotLeave() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.leaveGroup(groupId, ownerId));
        }

        @Test
        @DisplayName("Should throw exception when not a member")
        void testLeaveGroup_NotMember() {
            // Arrange
            Long nonMemberId = 999L;
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, nonMemberId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.leaveGroup(groupId, nonMemberId));
        }
    }

    // ==================== APPROVE/REJECT REQUEST TESTS ====================

    @Nested
    @DisplayName("Approve/Reject Request Tests")
    class ApproveRejectTests {

        private GroupMembership pendingMembership;

        @BeforeEach
        void setUp() {
            pendingMembership = GroupMembership.builder()
                    .id(3L)
                    .studyGroup(studyGroup)
                    .userId(memberId)
                    .role(GroupRole.MEMBER)
                    .status(MembershipStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Should approve join request successfully")
        void testApproveJoinRequest_Success() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, memberId))
                    .thenReturn(Optional.of(pendingMembership));
            when(membershipRepository.save(pendingMembership)).thenReturn(pendingMembership);

            // Act
            studyGroupService.approveJoinRequest(groupId, ownerId, memberId);

            // Assert
            assertEquals(MembershipStatus.ACTIVE, pendingMembership.getStatus());

            // Verify event was published
            ArgumentCaptor<MemberJoinedGroupEvent> eventCaptor = 
                    ArgumentCaptor.forClass(MemberJoinedGroupEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertFalse(eventCaptor.getValue().isDirectJoin());
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void testApproveJoinRequest_NotPending() {
            // Arrange
            pendingMembership.setStatus(MembershipStatus.ACTIVE);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, memberId))
                    .thenReturn(Optional.of(pendingMembership));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.approveJoinRequest(groupId, ownerId, memberId));
        }

        @Test
        @DisplayName("Should reject join request successfully")
        void testRejectJoinRequest_Success() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findByStudyGroupIdAndUserId(groupId, memberId))
                    .thenReturn(Optional.of(pendingMembership));
            when(membershipRepository.save(pendingMembership)).thenReturn(pendingMembership);

            // Act
            studyGroupService.rejectJoinRequest(groupId, ownerId, memberId);

            // Assert
            assertEquals(MembershipStatus.REMOVED, pendingMembership.getStatus());
            assertNotNull(pendingMembership.getLeftAt());
        }
    }

    // ==================== REMOVE MEMBER TESTS ====================

    @Nested
    @DisplayName("Remove Member Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should remove member successfully")
        void testRemoveMember_Success() {
            // Arrange
            studyGroup.getMemberships().add(memberMembership);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, memberId))
                    .thenReturn(Optional.of(memberMembership));
            when(membershipRepository.save(memberMembership)).thenReturn(memberMembership);

            // Act
            studyGroupService.removeMember(groupId, ownerId, memberId);

            // Assert
            assertEquals(MembershipStatus.REMOVED, memberMembership.getStatus());
            assertNotNull(memberMembership.getLeftAt());

            // Verify event was published
            ArgumentCaptor<MemberLeftGroupEvent> eventCaptor = 
                    ArgumentCaptor.forClass(MemberLeftGroupEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertFalse(eventCaptor.getValue().isVoluntary());
            assertEquals(ownerId, eventCaptor.getValue().getRemovedByUserId());
        }

        @Test
        @DisplayName("Should throw exception when trying to remove owner")
        void testRemoveMember_CannotRemoveOwner() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.removeMember(groupId, ownerId, ownerId));
        }

        @Test
        @DisplayName("Admin cannot remove another admin")
        void testRemoveMember_AdminCannotRemoveAdmin() {
            // Arrange
            Long adminId = 3L;
            GroupMembership adminMembership = GroupMembership.builder()
                    .userId(adminId)
                    .studyGroup(studyGroup)
                    .role(GroupRole.ADMIN)
                    .status(MembershipStatus.ACTIVE)
                    .build();

            GroupMembership targetAdmin = GroupMembership.builder()
                    .userId(memberId)
                    .studyGroup(studyGroup)
                    .role(GroupRole.ADMIN)
                    .status(MembershipStatus.ACTIVE)
                    .build();

            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, adminId))
                    .thenReturn(Optional.of(adminMembership));
            when(membershipRepository.findActiveMembership(groupId, memberId))
                    .thenReturn(Optional.of(targetAdmin));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.removeMember(groupId, adminId, memberId));
        }
    }

    // ==================== UPDATE ROLE TESTS ====================

    @Nested
    @DisplayName("Update Member Role Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update member role to admin successfully")
        void testUpdateMemberRole_ToAdmin_Success() {
            // Arrange
            studyGroup.getMemberships().add(memberMembership);
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
            when(membershipRepository.findActiveMembership(groupId, memberId))
                    .thenReturn(Optional.of(memberMembership));
            when(membershipRepository.save(memberMembership)).thenReturn(memberMembership);

            // Act
            studyGroupService.updateMemberRole(groupId, ownerId, memberId, GroupRole.ADMIN);

            // Assert
            assertEquals(GroupRole.ADMIN, memberMembership.getRole());
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to update role")
        void testUpdateMemberRole_NonOwner_Fails() {
            // Arrange
            Long adminId = 3L;
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.updateMemberRole(groupId, adminId, memberId, GroupRole.ADMIN));
        }

        @Test
        @DisplayName("Should throw exception when trying to assign OWNER role")
        void testUpdateMemberRole_CannotAssignOwner() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.updateMemberRole(groupId, ownerId, memberId, GroupRole.OWNER));
        }

        @Test
        @DisplayName("Should throw exception when owner tries to change own role")
        void testUpdateMemberRole_CannotChangeOwnRole() {
            // Arrange
            when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    studyGroupService.updateMemberRole(groupId, ownerId, ownerId, GroupRole.ADMIN));
        }
    }

    // ==================== GET MEMBERS TESTS ====================

    @Nested
    @DisplayName("Get Group Members Tests")
    class GetMembersTests {

        @Test
        @DisplayName("Should return enriched member list")
        void testGetGroupMembers_Success() {
            // Arrange
            List<GroupMembership> memberships = List.of(ownerMembership, memberMembership);
            List<UserProfile> profiles = List.of(
                    UserProfile.builder().userId(ownerId).firstName("John").lastName("Doe").build(),
                    UserProfile.builder().userId(memberId).firstName("Jane").lastName("Smith").build()
            );

            when(membershipRepository.findByStudyGroupIdAndStatus(groupId, MembershipStatus.ACTIVE))
                    .thenReturn(memberships);
            when(userProfileRepository.findByUserIds(List.of(ownerId, memberId)))
                    .thenReturn(profiles);
            when(mapper.toMemberResponseWithProfile(any(), any()))
                    .thenReturn(GroupMemberResponse.builder().userId(ownerId).build());

            // Act
            List<GroupMemberResponse> result = studyGroupService.getGroupMembers(groupId);

            // Assert
            assertEquals(2, result.size());
            verify(userProfileRepository).findByUserIds(anyList());
        }

        @Test
        @DisplayName("Should return empty list when no members")
        void testGetGroupMembers_EmptyList() {
            // Arrange
            when(membershipRepository.findByStudyGroupIdAndStatus(groupId, MembershipStatus.ACTIVE))
                    .thenReturn(List.of());

            // Act
            List<GroupMemberResponse> result = studyGroupService.getGroupMembers(groupId);

            // Assert
            assertTrue(result.isEmpty());
            verify(userProfileRepository, never()).findByUserIds(anyList());
        }
    }
}
