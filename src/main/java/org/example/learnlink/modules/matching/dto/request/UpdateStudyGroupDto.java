package org.example.learnlink.modules.matching.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing study group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudyGroupDto {

    /**
     * Updated group name
     */
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;

    /**
     * Updated description
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Updated max members count
     */
    @Min(value = 2, message = "Group must allow at least 2 members")
    @Max(value = 50, message = "Group cannot exceed 50 members")
    private Integer maxMembers;

    /**
     * Updated public/private setting
     */
    private Boolean isPublic;

    /**
     * Updated cover image URL
     */
    private String coverImageUrl;
}
