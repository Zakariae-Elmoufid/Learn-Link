package org.example.learnlink.modules.gamification.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPointsRequest {

    @NotBlank(message = "Action type is required")
    private String actionType;

    @NotNull(message = "Points is required")
    @Positive(message = "Points must be positive")
    private Integer points;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;


}