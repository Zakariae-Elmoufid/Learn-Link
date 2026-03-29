package org.example.learnlink.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for StudentSubject response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubjectResponse {
    private Long id;
    private String name;
}
