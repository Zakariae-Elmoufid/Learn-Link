package org.example.learnlink.modules.planner.dto;

import org.example.learnlink.modules.planner.entity.TaskPriority;
import org.example.learnlink.modules.planner.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for task response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TaskPriority priority;
    private TaskStatus status;
    private Boolean completed;
    private LocalDateTime completedAt;
    private String subject;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOverdue;
}
