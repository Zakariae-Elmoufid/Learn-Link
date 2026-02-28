package org.example.learnlink.modules.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.admin.dto.request.CreateModeratorRequest;
import org.example.learnlink.modules.admin.dto.request.UpdateModeratorPermissionsRequest;
import org.example.learnlink.modules.admin.dto.response.ModeratorPermissionsResponse;
import org.example.learnlink.modules.admin.dto.response.ModeratorResponse;
import org.example.learnlink.modules.admin.service.AdminModeratorService;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for moderator management operations.
 * Only accessible by ADMIN users.
 */
@RestController
@RequestMapping("/api/admin/moderators")
@RequiredArgsConstructor
@Tag(name = "Admin Moderator Management", description = "Endpoints for managing moderators")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModeratorController {

    private final AdminModeratorService adminModeratorService;

    @GetMapping
    @Operation(summary = "Get all moderators", description = "Retrieves a list of all moderators with their permissions")
    public ResponseEntity<List<ModeratorResponse>> getAllModerators() {
        List<ModeratorResponse> moderators = adminModeratorService.getAllModerators();
        return ResponseEntity.ok(moderators);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get moderator by user ID", description = "Retrieves a specific moderator's details including permissions")
    public ResponseEntity<ModeratorResponse> getModeratorByUserId(
            @Parameter(description = "User ID of the moderator") @PathVariable Long userId) {
        ModeratorResponse moderator = adminModeratorService.getModeratorByUserId(userId);
        return ResponseEntity.ok(moderator);
    }

    @GetMapping("/{userId}/permissions")
    @Operation(summary = "Get moderator permissions", description = "Retrieves current and available permissions for a moderator")
    public ResponseEntity<ModeratorPermissionsResponse> getModeratorPermissions(
            @Parameter(description = "User ID of the moderator") @PathVariable Long userId) {
        ModeratorPermissionsResponse permissions = adminModeratorService.getModeratorPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @PostMapping
    @Operation(summary = "Create moderator", description = "Assigns moderator role to a user with specified permissions")
    public ResponseEntity<ModeratorResponse> createModerator(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Valid @RequestBody CreateModeratorRequest request) {
        ModeratorResponse moderator = adminModeratorService.createModerator(admin.getId(), request);
        return ResponseEntity.ok(moderator);
    }

    @PutMapping("/{userId}/permissions")
    @Operation(summary = "Update moderator permissions", description = "Updates the permissions assigned to a moderator")
    public ResponseEntity<ModeratorResponse> updateModeratorPermissions(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Parameter(description = "User ID of the moderator") @PathVariable Long userId,
            @Valid @RequestBody UpdateModeratorPermissionsRequest request) {

        ModeratorResponse moderator = adminModeratorService.updateModeratorPermissions(admin.getId(), userId, request);
        return ResponseEntity.ok(moderator);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove moderator", description = "Removes moderator role from a user, reverting them to STUDENT role")
    public ResponseEntity<Void> removeModerator(
            @AuthenticationPrincipal User admin,
            @Parameter(description = "User ID of the moderator") @PathVariable Long userId,
            @Parameter(description = "Reason for removing moderator role") @RequestParam(required = false) String reason) {
        adminModeratorService.removeModerator(admin.getId(), userId, reason);
        return ResponseEntity.noContent().build();
    }
}
