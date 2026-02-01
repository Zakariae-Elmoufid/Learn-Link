package org.example.learnlink.modules.matching.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new study group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudyGroupDto {

    /**
     * Name of the study group
     */
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;

    /**
     * Description of the group
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Subject ID the group focuses on (optional)
     */
    private Long subjectId;

    /**
     * Maximum number of members (default: 10, max: 50)
     */
    @Min(value = 2, message = "Group must allow at least 2 members")
    @Max(value = 50, message = "Group cannot exceed 50 members")
    @Builder.Default
    private Integer maxMembers = 10;

    /**
     * Whether the group is public or private
     */
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * Cover image URL (optional)
     */
    private String coverImageUrl;
}
