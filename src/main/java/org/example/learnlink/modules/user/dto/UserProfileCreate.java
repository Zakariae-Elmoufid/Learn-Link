package org.example.learnlink.modules.user.dto;

import jakarta.validation.constraints.*;
import org.example.learnlink.modules.user.entity.AcademicLevel;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UserProfileCreate(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name cannot exceed 50 characters")
         String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name cannot exceed 50 characters")
         String lastName,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
         String bio,

        MultipartFile image,
        @NotEmpty(message = "At least one subject must be selected")
         List<Long> studentSubjectIds,

        @NotNull(message = "Academic level is required")
         AcademicLevel academicLevel

) {
}
