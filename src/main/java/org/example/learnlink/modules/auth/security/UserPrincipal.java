package org.example.learnlink.modules.auth.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified user principal for use in controllers.
 * Contains essential user information extracted from the authenticated user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {

    private Long id;
    private String email;
    private String username;
    private String role;

    /**
     * Create UserPrincipal from CustomUserDetails.
     */
    public static UserPrincipal from(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            return null;
        }
        return UserPrincipal.builder()
                .id(userDetails.getUser().getId())
                .email(userDetails.getUser().getEmail())
                .username(userDetails.getUser().getUsername())
                .role(userDetails.getUser().getRole().name())
                .build();
    }
}
