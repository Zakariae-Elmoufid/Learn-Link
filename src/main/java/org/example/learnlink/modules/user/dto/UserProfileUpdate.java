package org.example.learnlink.modules.user.dto;

import jakarta.validation.constraints.Size;
import org.example.learnlink.modules.user.entity.AcademicLevel;

import java.util.List;

/**
 * DTO for updating user profile.
 * All fields are optional - only provided fields will be updated.
 */
public record UserProfileUpdate(
        @Size(max = 50, message = "First name cannot exceed 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        String lastName,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,

        List<Long> studentSubjectIds,

        AcademicLevel academicLevel
) {
}
