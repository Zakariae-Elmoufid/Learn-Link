package org.example.learnlink.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a StudentSubject
 */
public record StudentSubjectRequest(
        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name cannot exceed 100 characters")
        String name
) {
}
