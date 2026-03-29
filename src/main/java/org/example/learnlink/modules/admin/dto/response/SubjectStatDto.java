package org.example.learnlink.modules.admin.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectStatDto {
    private String subject;
    private Long count;
    private Double percentage;
}
