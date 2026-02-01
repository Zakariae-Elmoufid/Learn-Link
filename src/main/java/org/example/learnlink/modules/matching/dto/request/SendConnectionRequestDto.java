package org.example.learnlink.modules.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for sending a new connection request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendConnectionRequestDto {

    /**
     * The ID of the user to connect with
     */
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    /**
     * Optional personal message to include with the request
     */
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
}
