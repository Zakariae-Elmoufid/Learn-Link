package org.example.learnlink.modules.admin.service;

import org.example.learnlink.modules.admin.dto.request.CreateModeratorRequest;
import org.example.learnlink.modules.admin.dto.request.UpdateModeratorPermissionsRequest;
import org.example.learnlink.modules.admin.dto.response.ModeratorPermissionsResponse;
import org.example.learnlink.modules.admin.dto.response.ModeratorResponse;

import java.util.List;

/**
 * Service interface for moderator management
 */
public interface AdminModeratorService {

    /**
     * Get all moderators
     * @return List of all moderators
     */
    List<ModeratorResponse> getAllModerators();

    /**
     * Get moderator by user ID
     * @param userId User ID of the moderator
     * @return Moderator details
     */
    ModeratorResponse getModeratorByUserId(Long userId);

    /**
     * Create a new moderator from an existing user
     * @param adminId ID of the admin performing the action
     * @param request Create moderator request
     * @return Created moderator details
     */
    ModeratorResponse createModerator(Long adminId, CreateModeratorRequest request);

    /**
     * Update moderator permissions
     * @param adminId ID of the admin performing the action
     * @param moderatorUserId User ID of the moderator
     * @param request Update permissions request
     * @return Updated moderator details
     */
    ModeratorResponse updateModeratorPermissions(Long adminId, Long moderatorUserId, UpdateModeratorPermissionsRequest request);

    /**
     * Remove moderator role from a user
     * @param adminId ID of the admin performing the action
     * @param moderatorUserId User ID of the moderator to demote
     * @param reason Reason for removing moderator role
     */
    void removeModerator(Long adminId, Long moderatorUserId, String reason);

    /**
     * Get moderator permissions with available permissions list
     * @param userId User ID of the moderator
     * @return Permissions response
     */
    ModeratorPermissionsResponse getModeratorPermissions(Long userId);
}
