package org.example.learnlink.modules.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.learnlink.modules.matching.entity.enums.GroupStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for study group information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupResponse {

    private Long id;

    private String name;

    private String description;

    private Long subjectId;

    private String subjectName;

    private Long ownerId;

    private String ownerName;

    private Integer maxMembers;

    private Integer currentMemberCount;

    private GroupStatus status;

    private Boolean isPublic;

    private String coverImageUrl;

    private LocalDateTime createdAt;

    /**
     * Whether the current user is a member
     */
    private Boolean isMember;

    /**
     * Whether the current user is an admin
     */
    private Boolean isAdmin;

    /**
     * Whether the current user is the owner
     */
    private Boolean isOwner;

    /**
     * Whether the current user has a pending join request
     */
    private Boolean hasPendingRequest;

    /**
     * List of members (only included in detailed view)
     */
    private List<GroupMemberResponse> members;
}
