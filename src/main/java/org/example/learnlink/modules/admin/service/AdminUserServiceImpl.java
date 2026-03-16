package org.example.learnlink.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.common.exception.ResourceNotFoundException;
import org.example.learnlink.modules.admin.dto.request.UserFilterRequest;
import org.example.learnlink.modules.admin.dto.response.AdminUserDto;
import org.example.learnlink.modules.admin.dto.response.PageResponse;
import org.example.learnlink.modules.auth.entity.User;
import org.example.learnlink.modules.auth.entity.UserRole;
import org.example.learnlink.modules.auth.repository.UserRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import org.example.learnlink.modules.gamification.entity.UserScore;
import org.example.learnlink.modules.gamification.repository.UserScoreRepository;
import org.example.learnlink.modules.matching.repository.ConnectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AdminUserService
 * Provides user management operations for admin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserScoreRepository userScoreRepository;
    private final PostRepository postRepository;
    private final ConnectionRepository connectionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserDto> getAllUsers(UserFilterRequest filterRequest) {
        log.info("Fetching users with filters: role={}, active={}, search={}", 
                filterRequest.getRole(), filterRequest.getActive(), filterRequest.getSearch());

        // Build sort
        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("asc") 
                        ? Sort.Direction.ASC 
                        : Sort.Direction.DESC,
                filterRequest.getSortBy()
        );
        String search = filterRequest.getSearch();
        if (search != null && search.isBlank()) {
            search = null;
        }
        // Build pageable
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                sort
        );

        // Parse role if provided
        UserRole role = null;
        if (filterRequest.getRole() != null && !filterRequest.getRole().isEmpty()) {
            try {
                role = UserRole.valueOf(filterRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}", filterRequest.getRole());
            }
        }

        // Fetch users with filters
        Page<User> userPage = userRepository.findAllWithFilters(
                role,
                filterRequest.getActive(),
                search,
                pageable
        );

        // Map to DTOs with full stats
        List<AdminUserDto> userDtos = userPage.getContent().stream()
                .map(this::mapToFullDto)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserDto>builder()
                .content(userDtos)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUserById(Long userId) {
        log.info("Fetching user details for ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToFullDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto activateUser(Long userId) {
        log.info("Activating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(true);
        user = userRepository.save(user);
        
        log.info("User {} activated successfully", userId);
        return mapToBasicDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(false);
        user = userRepository.save(user);
        
        log.info("User {} deactivated successfully", userId);
        return mapToBasicDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto changeUserRole(Long userId, String newRole) {
        log.info("Changing role for user {} to {}", userId, newRole);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            UserRole role = UserRole.valueOf(newRole.toUpperCase());
            user.setRole(role);
            user = userRepository.save(user);
            
            log.info("User {} role changed to {} successfully", userId, newRole);
            return mapToBasicDto(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
    }

    /**
     * Map user to basic DTO (for list view)
     */
    private AdminUserDto mapToBasicDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Map user to full DTO with additional stats (for detail view)
     */
    private AdminUserDto mapToFullDto(User user) {
        AdminUserDto dto = mapToBasicDto(user);

        // Fetch gamification stats
        userScoreRepository.findByUserId(user.getId())
                .ifPresent(score -> {
                    dto.setTotalPoints(score.getTotalPoints());
                    dto.setLevel(score.getLevel());
                });

        // Fetch posts count
        try {
            long postsCount = postRepository.findByUserId(user.getId(), PageRequest.of(0, 1)).getTotalElements();
            dto.setPostsCount(postsCount);
        } catch (Exception e) {
            log.warn("Failed to fetch posts count for user {}: {}", user.getId(), e.getMessage());
            dto.setPostsCount(0L);
        }

        // Fetch connections count
        try {
            long connectionsCount = connectionRepository.findActiveByUserId(user.getId()).size();
            dto.setConnectionsCount(connectionsCount);
        } catch (Exception e) {
            log.warn("Failed to fetch connections count for user {}: {}", user.getId(), e.getMessage());
            dto.setConnectionsCount(0L);
        }

        return dto;
    }
}
