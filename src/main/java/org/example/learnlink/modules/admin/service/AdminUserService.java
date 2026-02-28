package org.example.learnlink.modules.admin.service;

import org.example.learnlink.modules.admin.dto.request.UserFilterRequest;
import org.example.learnlink.modules.admin.dto.response.AdminUserDto;
import org.example.learnlink.modules.admin.dto.response.PageResponse;

/**
 * Service for admin user management operations
 */
public interface AdminUserService {
    
    /**
     * Get paginated list of users with optional filters
     * @param filterRequest Filter and pagination parameters
     * @return Paginated list of users
     */
    PageResponse<AdminUserDto> getAllUsers(UserFilterRequest filterRequest);
    
    /**
     * Get a single user by ID with full details
     * @param userId User ID
     * @return User details
     */
    AdminUserDto getUserById(Long userId);
    
    /**
     * Activate a user account
     * @param userId User ID to activate
     * @return Updated user details
     */
    AdminUserDto activateUser(Long userId);
    
    /**
     * Deactivate a user account
     * @param userId User ID to deactivate
     * @return Updated user details
     */
    AdminUserDto deactivateUser(Long userId);
    
    /**
     * Change user role
     * @param userId User ID
     * @param newRole New role to assign
     * @return Updated user details
     */
    AdminUserDto changeUserRole(Long userId, String newRole);
}
