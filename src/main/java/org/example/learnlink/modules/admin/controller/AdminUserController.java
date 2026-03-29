package org.example.learnlink.modules.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.admin.dto.request.UserFilterRequest;
import org.example.learnlink.modules.admin.dto.response.AdminUserDto;
import org.example.learnlink.modules.admin.dto.response.PageResponse;
import org.example.learnlink.modules.admin.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for admin user management
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@Tag(name = "Admin - User Management", description = "Endpoints for managing users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated list of users with optional filters")
    public ResponseEntity<PageResponse<AdminUserDto>> getAllUsers(
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Search by email or username") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Admin: Fetching users - page={}, size={}, role={}, active={}, search={}", 
                page, size, role, active, search);

        UserFilterRequest filterRequest = UserFilterRequest.builder()
                .role(role)
                .active(active)
                .search(search)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        PageResponse<AdminUserDto> response = adminUserService.getAllUsers(filterRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get detailed information about a specific user")
    public ResponseEntity<AdminUserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        log.info("Admin: Fetching user details for ID: {}", userId);
        AdminUserDto user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user", description = "Activate a user account")
    public ResponseEntity<AdminUserDto> activateUser(
            @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        log.info("Admin: Activating user: {}", userId);
        AdminUserDto user = adminUserService.activateUser(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    public ResponseEntity<AdminUserDto> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long userId
    ) {
        log.info("Admin: Deactivating user: {}", userId);
        AdminUserDto user = adminUserService.deactivateUser(userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role", description = "Change the role of a user")
    public ResponseEntity<AdminUserDto> changeUserRole(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "New role") @RequestParam String role
    ) {
        log.info("Admin: Changing role for user {} to {}", userId, role);
        AdminUserDto user = adminUserService.changeUserRole(userId, role);
        return ResponseEntity.ok(user);
    }
}
