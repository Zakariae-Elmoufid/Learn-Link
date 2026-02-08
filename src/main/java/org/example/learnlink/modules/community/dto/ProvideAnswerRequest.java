package org.example.learnlink.modules.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for providing an answer to a question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvideAnswerRequest {

    @NotBlank(message = "Content cannot be blank")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;
}

