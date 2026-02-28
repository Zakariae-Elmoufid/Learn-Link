package org.example.learnlink.modules.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering users in admin view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    
    /**
     * Filter by role: ADMIN, MODERATOR, STUDENT
     */
    private String role;
    
    /**
     * Filter by active status
     */
    private Boolean active;
    
    /**
     * Search by email or username
     */
    private String search;
    
    /**
     * Page number (0-indexed)
     */
    @Builder.Default
    private Integer page = 0;
    
    /**
     * Page size
     */
    @Builder.Default
    private Integer size = 20;
    
    /**
     * Sort field: id, email, username, createdAt, active
     */
    @Builder.Default
    private String sortBy = "createdAt";
    
    /**
     * Sort direction: asc, desc
     */
    @Builder.Default
    private String sortDirection = "desc";
}
