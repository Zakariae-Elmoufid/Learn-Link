package org.example.learnlink.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin user list response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    
    private Long id;
    private String email;
    private String username;
    private String role;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    
    // Additional computed fields
    private Integer totalPoints;
    private Integer level;
    private Long postsCount;
    private Long connectionsCount;
}
