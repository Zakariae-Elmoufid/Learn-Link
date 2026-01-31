package org.example.learnlink.modules.planner.mapper;

import org.example.learnlink.modules.planner.dto.TaskResponse;
import org.example.learnlink.modules.planner.entity.Task;
import org.mapstruct.*;

/**
 * MapStruct mapper for Task entity to TaskResponse DTO
 * Automatically generates implementation at compile time
 */
@Mapper(componentModel = "spring", // Spring bean
        unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    /**
     * Map Task entity to TaskResponse DTO
     * 
     * @param task The task entity
     * @return TaskResponse DTO
     */
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    TaskResponse toResponse(Task task);

    /**
     * Map list of Task entities to list of TaskResponse DTOs
     * 
     * @param tasks List of task entities
     * @return List of TaskResponse DTOs
     */
    java.util.List<TaskResponse> toResponseList(java.util.List<Task> tasks);
}
