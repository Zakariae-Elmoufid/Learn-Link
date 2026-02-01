package org.example.learnlink.modules.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.matching.entity.enums.GroupRole;
import org.example.learnlink.modules.matching.entity.enums.MembershipStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for group member information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberResponse {

    private Long userId;

    private String firstName;

    private String lastName;

    private String profilePictureUrl;

    private GroupRole role;

    private MembershipStatus status;

    private LocalDateTime joinedAt;

    /**
     * Computed display name
     */
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "User " + userId;
    }
}
