package org.example.learnlink.modules.matching.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.example.learnlink.modules.matching.dto.request.CreateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.request.UpdateStudyGroupDto;
import org.example.learnlink.modules.matching.dto.response.GroupMemberResponse;
import org.example.learnlink.modules.matching.dto.response.StudyGroupResponse;
import org.example.learnlink.modules.matching.entity.enums.GroupRole;
import org.example.learnlink.modules.matching.service.IStudyGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for study group operations.
 * Provides endpoints for creating, managing, and discovering study groups.
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final IStudyGroupService studyGroupService;

    // ==================== Group CRUD ====================

    /**
     * Create a new study group.
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<StudyGroupResponse> createGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateStudyGroupDto dto) {
        Long userId = userDetails.getId();
        StudyGroupResponse response = studyGroupService.createGroup(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get group details.
     * GET /api/groups/{groupId}
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroupResponse> getGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        StudyGroupResponse response = studyGroupService.getGroup(groupId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get group details with all members.
     * GET /api/groups/{groupId}/full
     */
    @GetMapping("/{groupId}/full")
    public ResponseEntity<StudyGroupResponse> getGroupWithMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        StudyGroupResponse response = studyGroupService.getGroupWithMembers(groupId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a study group.
     * PUT /api/groups/{groupId}
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroupResponse> updateGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateStudyGroupDto dto) {
        Long userId = userDetails.getId();
        StudyGroupResponse response = studyGroupService.updateGroup(groupId, userId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete/disband a study group.
     * DELETE /api/groups/{groupId}
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        studyGroupService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Discovery ====================

    /**
     * Discover public groups.
     * GET /api/groups/discover?page=0&size=10
     */
    @GetMapping("/discover")
    public ResponseEntity<Page<StudyGroupResponse>> discoverGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<StudyGroupResponse> groups = studyGroupService.discoverGroups(pageable);
        return ResponseEntity.ok(groups);
    }

    /**
     * Search groups by keyword.
     * GET /api/groups/search?keyword=java&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<StudyGroupResponse>> searchGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<StudyGroupResponse> groups = studyGroupService.searchGroups(keyword, pageable);
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups by subject.
     * GET /api/groups/subject/{subjectId}
     */
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<StudyGroupResponse>> getGroupsBySubject(
            @PathVariable Long subjectId) {

        List<StudyGroupResponse> groups = studyGroupService.getGroupsBySubject(subjectId);
        return ResponseEntity.ok(groups);
    }

    // ==================== User's Groups ====================

    /**
     * Get groups the current user is a member of.
     * GET /api/groups/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<StudyGroupResponse>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<StudyGroupResponse> groups = studyGroupService.getMyGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups owned by the current user.
     * GET /api/groups/owned
     */
    @GetMapping("/owned")
    public ResponseEntity<List<StudyGroupResponse>> getOwnedGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<StudyGroupResponse> groups = studyGroupService.getOwnedGroups(userId);
        return ResponseEntity.ok(groups);
    }

    // ==================== Membership ====================

    /**
     * Join a public group.
     * POST /api/groups/{groupId}/join
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> joinGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        studyGroupService.joinGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Request to join a private group.
     * POST /api/groups/{groupId}/request-join
     */
    @PostMapping("/{groupId}/request-join")
    public ResponseEntity<Void> requestToJoin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        studyGroupService.requestToJoin(groupId, userId);
        return ResponseEntity.accepted().build();
    }

    /**
     * Leave a group.
     * POST /api/groups/{groupId}/leave
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        studyGroupService.leaveGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get group members.
     * GET /api/groups/{groupId}/members
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable Long groupId) {

        List<GroupMemberResponse> members = studyGroupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    // ==================== Admin Actions ====================

    /**
     * Get pending join requests (admin only).
     * GET /api/groups/{groupId}/requests
     */
    @GetMapping("/{groupId}/requests")
    public ResponseEntity<List<GroupMemberResponse>> getPendingRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = userDetails.getId();
        List<GroupMemberResponse> requests = studyGroupService.getPendingRequests(groupId, userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a join request.
     * POST /api/groups/{groupId}/requests/{requesterId}/approve
     */
    @PostMapping("/{groupId}/requests/{requesterId}/approve")
    public ResponseEntity<Void> approveJoinRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long requesterId) {
        Long userId = userDetails.getId();
        studyGroupService.approveJoinRequest(groupId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a join request.
     * POST /api/groups/{groupId}/requests/{requesterId}/reject
     */
    @PostMapping("/{groupId}/requests/{requesterId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long requesterId) {
        Long userId = userDetails.getId();
        studyGroupService.rejectJoinRequest(groupId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove a member from the group.
     * DELETE /api/groups/{groupId}/members/{memberId}
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        Long userId = userDetails.getId();
        studyGroupService.removeMember(groupId, userId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a member's role.
     * PUT /api/groups/{groupId}/members/{memberId}/role
     */
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @RequestParam GroupRole role) {
        Long userId = userDetails.getId();
        studyGroupService.updateMemberRole(groupId, userId, memberId, role);
        return ResponseEntity.ok().build();
    }
}
