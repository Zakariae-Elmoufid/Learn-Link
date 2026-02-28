package org.example.learnlink.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.admin.dto.request.CreateModeratorRequest;
import org.example.learnlink.modules.admin.dto.request.UpdateModeratorPermissionsRequest;
import org.example.learnlink.modules.admin.dto.response.ModeratorPermissionsResponse;
import org.example.learnlink.modules.admin.dto.response.ModeratorResponse;
import org.example.learnlink.modules.admin.entity.ModeratorPermission;
import org.example.learnlink.modules.admin.entity.ModeratorPermissionEntity;
import org.example.learnlink.modules.admin.repository.ModeratorPermissionRepository;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AdminModeratorService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminModeratorServiceImpl implements AdminModeratorService {

    private final UserRepository userRepository;
    private final ModeratorPermissionRepository moderatorPermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModeratorResponse> getAllModerators() {
        log.info("Fetching all moderators");
        
        List<ModeratorPermissionEntity> moderatorPermissions = moderatorPermissionRepository.findAllOrderByAssignedAtDesc();
        
        return moderatorPermissions.stream()
                .map(this::mapToModeratorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModeratorResponse getModeratorByUserId(Long userId) {
        log.info("Fetching moderator by user ID: {}", userId);
        
        ModeratorPermissionEntity permissions = moderatorPermissionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found with user ID: " + userId));
        
        return mapToModeratorResponse(permissions);
    }

    @Override
    @Transactional
    public ModeratorResponse createModerator(Long adminId, CreateModeratorRequest request) {
        log.info("Admin {} creating moderator for user {}", adminId, request.getUserId());
        
        // Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        
        // Check if user is already a moderator
        if (moderatorPermissionRepository.existsByUserId(request.getUserId())) {
            throw new IllegalStateException("User is already a moderator");
        }
        
        // Check if user is an admin (cannot demote admin to moderator)
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Cannot assign moderator role to an admin");
        }
        
        // Update user role to MODERATOR
        user.setRole(UserRole.MODERATOR);
        userRepository.save(user);
        
        // Create moderator permissions
        ModeratorPermissionEntity permissions = ModeratorPermissionEntity.builder()
                .userId(request.getUserId())
                .permissions(request.getPermissions())
                .assignedBy(adminId)
                .assignedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();
        
        permissions = moderatorPermissionRepository.save(permissions);
        
        log.info("Moderator created successfully for user {}", request.getUserId());
        return mapToModeratorResponse(permissions);
    }

    @Override
    @Transactional
    public ModeratorResponse updateModeratorPermissions(Long adminId, Long moderatorUserId, UpdateModeratorPermissionsRequest request) {
        log.info("Admin {} updating permissions for moderator {}", adminId, moderatorUserId);
        
        ModeratorPermissionEntity permissions = moderatorPermissionRepository.findByUserId(moderatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found with user ID: " + moderatorUserId));
        
        // Update permissions
        permissions.setPermissions(request.getPermissions());
        permissions.setUpdatedAt(LocalDateTime.now());
        
        // Append reason to notes if provided
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            String existingNotes = permissions.getNotes() != null ? permissions.getNotes() : "";
            String timestamp = LocalDateTime.now().toString();
            permissions.setNotes(existingNotes + "\n[" + timestamp + "] Permission update: " + request.getReason());
        }
        
        permissions = moderatorPermissionRepository.save(permissions);
        
        log.info("Moderator permissions updated successfully for user {}", moderatorUserId);
        return mapToModeratorResponse(permissions);
    }

    @Override
    @Transactional
    public void removeModerator(Long adminId, Long moderatorUserId, String reason) {
        log.info("Admin {} removing moderator role from user {}, reason: {}", adminId, moderatorUserId, reason);
        
        // Verify moderator exists
        if (!moderatorPermissionRepository.existsByUserId(moderatorUserId)) {
            throw new ResourceNotFoundException("Moderator not found with user ID: " + moderatorUserId);
        }
        
        // Get user and update role back to STUDENT
        User user = userRepository.findById(moderatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + moderatorUserId));
        
        user.setRole(UserRole.STUDENT);
        userRepository.save(user);
        
        // Delete moderator permissions
        moderatorPermissionRepository.deleteByUserId(moderatorUserId);
        
        log.info("Moderator role removed successfully from user {}", moderatorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ModeratorPermissionsResponse getModeratorPermissions(Long userId) {
        log.info("Fetching permissions for moderator: {}", userId);
        
        ModeratorPermissionEntity permissions = moderatorPermissionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found with user ID: " + userId));
        
        // Get all available permissions
        Set<ModeratorPermission> allPermissions = EnumSet.allOf(ModeratorPermission.class);
        
        return ModeratorPermissionsResponse.builder()
                .userId(userId)
                .currentPermissions(permissions.getPermissions())
                .availablePermissions(allPermissions)
                .lastUpdated(permissions.getUpdatedAt() != null ? permissions.getUpdatedAt() : permissions.getAssignedAt())
                .build();
    }

    /**
     * Map ModeratorPermissionEntity to ModeratorResponse
     */
    private ModeratorResponse mapToModeratorResponse(ModeratorPermissionEntity permissions) {
        ModeratorResponse.ModeratorResponseBuilder builder = ModeratorResponse.builder()
                .id(permissions.getId())
                .userId(permissions.getUserId())
                .permissions(permissions.getPermissions())
                .assignedByUserId(permissions.getAssignedBy())
                .assignedAt(permissions.getAssignedAt())
                .updatedAt(permissions.getUpdatedAt())
                .notes(permissions.getNotes());
        
        // Fetch user details
        userRepository.findById(permissions.getUserId()).ifPresent(user -> {
            builder.username(user.getUsername());
            builder.email(user.getEmail());
            builder.active(user.getActive());
        });
        
        // Fetch assigner details
        userRepository.findById(permissions.getAssignedBy()).ifPresent(admin -> {
            builder.assignedByUsername(admin.getUsername());
        });
        
        return builder.build();
    }
}
